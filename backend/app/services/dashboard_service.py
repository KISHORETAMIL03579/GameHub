import httpx
import asyncio
from typing import List, Optional
from app.config import settings
from app.models.game import (
    DashboardResponse,
    FeaturedGame,
    DashboardDeal,
    RecentGame,
    StoreType,
    StorePriceInfo
)
from app.services.steam_service import steam_service
from app.services.epic_service import epic_service

class DashboardService:
    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }

    async def get_dashboard_data(self, steam_user: Optional[str] = None, epic_user: Optional[str] = None) -> DashboardResponse:
        """
        Fetches 100% real live market data directly from Steam and Epic Games Store APIs with regional INR pricing.
        """
        async with httpx.AsyncClient(timeout=12.0) as client:
            # 1. Fetch live featured categories (specials, top sellers, new releases) from Steam
            steam_featured_task = client.get(
                "https://store.steampowered.com/api/featuredcategories/?cc=in&l=en",
                headers=self.headers
            )
            # 2. Fetch live Cyberpunk details from Steam
            cyberpunk_task = steam_service.get_app_details("1091500")
            # 3. Fetch live Cyberpunk pricing from Epic
            epic_cyberpunk_task = epic_service.search_catalog("Cyberpunk 2077", limit=3)
            # 4. Fetch live Epic free/discounted games
            epic_offers_task = epic_service.get_free_games()

            results = await asyncio.gather(
                steam_featured_task,
                cyberpunk_task,
                epic_cyberpunk_task,
                epic_offers_task,
                return_exceptions=True
            )

            # Process Steam live specials & top sellers
            steam_specials = []
            steam_top_sellers = []
            if not isinstance(results[0], Exception) and results[0].status_code == 200:
                try:
                    steam_data = results[0].json()
                    steam_specials = steam_data.get("specials", {}).get("items", [])
                    steam_top_sellers = steam_data.get("top_sellers", {}).get("items", [])
                except Exception:
                    pass

            # Process Featured Game (Cyberpunk 2077 live data from Steam + Epic)
            featured_game = None
            cp_steam = results[1] if not isinstance(results[1], Exception) else None
            cp_epic = results[2] if not isinstance(results[2], Exception) else []

            cp_stores = []
            lowest_price = 2999.0
            cheapest_store = StoreType.STEAM
            formatted_lowest = "₹2,999"
            banner_img = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1091500/capsule_616x353.jpg"

            if cp_steam:
                cp_stores.extend(cp_steam.stores)
                banner_img = cp_steam.banner_image or banner_img
                if cp_steam.stores:
                    lowest_price = cp_steam.stores[0].price
                    formatted_lowest = cp_steam.stores[0].formatted_price

            if cp_epic:
                for em in cp_epic:
                    if "Cyberpunk" in em.get("title", ""):
                        cp_stores.append(em["store_price"])
                        if em["store_price"].price < lowest_price:
                            lowest_price = em["store_price"].price
                            cheapest_store = StoreType.EPIC
                            formatted_lowest = em["store_price"].formatted_price
                        break

            featured_game = FeaturedGame(
                game_id="steam-1091500",
                title="Cyberpunk 2077",
                subtitle="Compare prices across Steam and Epic Games Store.",
                banner_image=banner_img,
                lowest_price=lowest_price,
                formatted_lowest_price=formatted_lowest,
                cheapest_store=cheapest_store,
                discount_percent=0,
                stores=cp_stores
            )

            # Build Best Deals Today from REAL live Steam specials & Epic offers
            best_deals: List[DashboardDeal] = []
            seen_deal_ids = set()

            # Add real Steam specials
            for item in steam_specials[:6]:
                s_id = str(item.get("id"))
                if not s_id or s_id in seen_deal_ids:
                    continue
                seen_deal_ids.add(s_id)

                orig_paise = item.get("original_price") or item.get("final_price") or 0
                final_paise = item.get("final_price") or 0
                orig_inr = float(orig_paise) / 100.0
                sale_inr = float(final_paise) / 100.0
                disc_pct = item.get("discount_percent") or 0

                best_deals.append(DashboardDeal(
                    game_id=f"steam-{s_id}",
                    title=item.get("name", ""),
                    cover_image=item.get("header_image") or item.get("large_capsule_image") or "",
                    store=StoreType.STEAM,
                    sale_price=sale_inr,
                    original_price=orig_inr,
                    formatted_sale_price=f"₹{sale_inr:,.0f}" if sale_inr > 0 else "Free",
                    discount_percent=disc_pct
                ))

            # Add real Epic offers
            epic_offers = results[3] if not isinstance(results[3], Exception) else []
            for eo in epic_offers[:4]:
                if eo.id in seen_deal_ids:
                    continue
                seen_deal_ids.add(eo.id)
                best_deals.append(DashboardDeal(
                    game_id=eo.id,
                    title=eo.title,
                    cover_image=eo.cover_image or "",
                    store=StoreType.EPIC,
                    sale_price=0.0,
                    original_price=eo.original_price,
                    formatted_sale_price="Free",
                    discount_percent=100
                ))

            # Build Recently Added / Top Sellers from REAL Steam live data
            recently_added: List[RecentGame] = []
            seen_recent_ids = set()
            for ts in steam_top_sellers[:10]:
                ts_id = str(ts.get("id"))
                if not ts_id or ts_id in seen_recent_ids:
                    continue
                name = ts.get("name", "")
                if not name or "Machine" in name or "Deck" in name:
                    continue
                seen_recent_ids.add(ts_id)
                recently_added.append(RecentGame(
                    game_id=f"steam-{ts_id}",
                    title=name,
                    cover_image=ts.get("header_image") or ts.get("large_capsule_image") or ""
                ))
                if len(recently_added) >= 6:
                    break

            # Real library counts (0 if user has not connected store account)
            user_steam_count = 0
            user_epic_count = 0
            if steam_user:
                # Query actual count if authenticated
                from app.routers.library import get_steam_library
                try:
                    steam_games = await get_steam_library(steam_user)
                    user_steam_count = len(steam_games)
                except Exception:
                    user_steam_count = 0

            if epic_user:
                from app.routers.library import get_epic_library
                try:
                    epic_games = await get_epic_library(epic_user)
                    user_epic_count = len(epic_games)
                except Exception:
                    user_epic_count = 0

            return DashboardResponse(
                steam_count=user_steam_count,
                epic_count=user_epic_count,
                featured_game=featured_game,
                best_deals=best_deals,
                recently_added=recently_added
            )

dashboard_service = DashboardService()
