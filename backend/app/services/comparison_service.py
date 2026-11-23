import asyncio
from typing import List, Optional
from app.models.game import (
    GameSummary,
    PriceComparisonResponse,
    StorePriceInfo,
    StoreType,
    GameDetails
)
from app.services.normalizer import normalize_title, titles_match
from app.services.steam_service import steam_service
from app.services.epic_service import epic_service

class ComparisonService:
    @staticmethod
    def compare_store_prices(stores: List[StorePriceInfo]) -> tuple[Optional[StoreType], float, Optional[str]]:
        """
        Determines the cheapest store, potential savings in INR, and a recommendation verdict.
        """
        if not stores:
            return None, 0.0, "No store pricing available."

        valid_stores = [s for s in stores if s.in_stock]
        if not valid_stores:
            return None, 0.0, "Game currently out of stock or unavailable."

        # Sort by active price ascending
        sorted_stores = sorted(valid_stores, key=lambda s: s.price)
        cheapest = sorted_stores[0]

        potential_savings = 0.0
        if len(sorted_stores) > 1:
            highest_price = max(s.price for s in sorted_stores)
            potential_savings = max(0.0, highest_price - cheapest.price)

        # Smart recommendation
        if cheapest.is_free:
            recommendation = f"Claim free on {cheapest.store.value} right now!"
        elif potential_savings > 0:
            recommendation = f"Buy on {cheapest.store.value} and save ₹{potential_savings:,.0f} compared to other stores!"
        elif cheapest.discount_percent > 0:
            recommendation = f"On sale at {cheapest.discount_percent}% off on {cheapest.store.value}."
        else:
            recommendation = f"Available at standard price on {cheapest.store.value}."

        return cheapest.store, potential_savings, recommendation

    async def aggregate_search(self, query: str, limit: int = 20, store: Optional[str] = None) -> List[GameSummary]:
        """
        Searches Steam and Epic concurrently with full fault tolerance and regional INR normalization.
        """
        fetch_steam = store is None or store.lower() in ["all", "steam"]
        fetch_epic = store is None or store.lower() in ["all", "epic"]

        steam_task = steam_service.search_games(query, limit=limit) if fetch_steam else asyncio.sleep(0, result=[])
        epic_task = epic_service.search_catalog(query, limit=limit) if fetch_epic else asyncio.sleep(0, result=[])

        results = await asyncio.gather(steam_task, epic_task, return_exceptions=True)

        steam_results = results[0] if not isinstance(results[0], Exception) else []
        epic_results = results[1] if not isinstance(results[1], Exception) else []

        combined: List[GameSummary] = []
        matched_epic_indices = set()

        # Iterate over Steam results and try to find matching Epic counterpart
        for s_item in steam_results:
            title = s_item["title"]
            stores = [s_item["store_price"]]
            cover_image = s_item["cover_image"]

            for idx, e_item in enumerate(epic_results):
                if idx in matched_epic_indices:
                    continue
                if titles_match(title, e_item["title"]):
                    stores.append(e_item["store_price"])
                    matched_epic_indices.add(idx)
                    break

            cheapest_store, potential_savings, _ = self.compare_store_prices(stores)
            lowest_price = min(s.price for s in stores) if stores else None
            max_orig_price = max(s.original_price for s in stores) if stores else None
            max_discount = max(s.discount_percent for s in stores) if stores else 0

            combined.append(GameSummary(
                game_id=s_item["id"],
                title=title,
                cover_image=cover_image,
                cheapest_store=cheapest_store,
                lowest_price=lowest_price,
                original_price=max_orig_price,
                highest_discount_percent=max_discount,
                formatted_lowest_price=f"₹{lowest_price:,.0f}" if lowest_price is not None and lowest_price > 0 else ("Free" if lowest_price == 0 else "N/A"),
                stores=stores
            ))

        # Add remaining unmatched Epic results
        for idx, e_item in enumerate(epic_results):
            if idx not in matched_epic_indices:
                stores = [e_item["store_price"]]
                cheapest_store, _, _ = self.compare_store_prices(stores)
                p = e_item["store_price"].price
                combined.append(GameSummary(
                    game_id=e_item["id"],
                    title=e_item["title"],
                    cover_image=e_item["cover_image"],
                    cheapest_store=cheapest_store,
                    lowest_price=p,
                    original_price=e_item["store_price"].original_price,
                    highest_discount_percent=e_item["store_price"].discount_percent,
                    formatted_lowest_price=f"₹{p:,.0f}" if p > 0 else "Free",
                    stores=stores
                ))

        return combined[:limit]

comparison_service = ComparisonService()
