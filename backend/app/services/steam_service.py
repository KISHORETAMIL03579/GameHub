import httpx
from typing import List, Optional, Dict, Any
from app.config import settings
from app.models.game import StorePriceInfo, StoreType, GameSummary, GameDetails

class SteamService:
    def __init__(self):
        self.search_url = settings.STEAM_STORE_SEARCH_URL
        self.appdetails_url = f"{settings.STEAM_STORE_BASE_URL}/appdetails"
        self.headers = {
            "User-Agent": "GameHub/1.0 (Android; Valve/Steam Client Aggregator)"
        }

    async def search_games(self, query: str, limit: int = 15) -> List[Dict[str, Any]]:
        """
        Searches Steam for games matching query with Indian region pricing (cc=in).
        """
        params = {
            "term": query,
            "l": "english",
            "cc": settings.DEFAULT_COUNTRY_CODE,
            "mode": "game"
        }
        
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                resp = await client.get(self.search_url, params=params, headers=self.headers)
                if resp.status_code == 200:
                    data = resp.json()
                    items = data.get("items", [])[:limit]
                    results = []
                    for item in items:
                        app_id = str(item.get("id"))
                        price_info = item.get("price", {})
                        
                        # Steam returns price in paise (100 paise = 1 INR) or None if free/unpriced
                        initial_paise = price_info.get("initial", 0) or 0
                        final_paise = price_info.get("final", 0) or 0
                        
                        is_free = item.get("type") == "free" or (final_paise == 0 and initial_paise == 0)
                        
                        price = float(final_paise) / 100.0
                        original_price = float(initial_paise) / 100.0 if initial_paise else price
                        discount = 0
                        if original_price > 0 and price < original_price:
                            discount = int(round((original_price - price) / original_price * 100))
                            
                        tiny_img = item.get("tiny_image", "")
                        header_img = f"https://cdn.cloudflare.steamstatic.com/steam/apps/{app_id}/header.jpg"
                        
                        store_price = StorePriceInfo(
                            store=StoreType.STEAM,
                            store_item_id=app_id,
                            store_url=f"https://store.steampowered.com/app/{app_id}/",
                            price=price,
                            original_price=original_price,
                            discount_percent=discount,
                            currency="INR",
                            is_free=is_free,
                            in_stock=True,
                            formatted_price=f"₹{price:,.0f}" if not is_free else "Free",
                            formatted_original_price=f"₹{original_price:,.0f}" if original_price > 0 else "₹0"
                        )
                        
                        results.append({
                            "id": f"steam-{app_id}",
                            "title": item.get("name", ""),
                            "cover_image": header_img or tiny_img,
                            "store_price": store_price
                        })
                    return results
            except Exception as e:
                print(f"Error querying Steam search: {e}")
                return []
        return []

    async def get_app_details(self, app_id: str) -> Optional[GameDetails]:
        """
        Fetches detailed game data from Steam Storefront API with INR pricing.
        """
        params = {
            "appids": app_id,
            "cc": settings.DEFAULT_COUNTRY_CODE,
            "l": "english"
        }
        
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                resp = await client.get(self.appdetails_url, params=params, headers=self.headers)
                if resp.status_code == 200:
                    data = resp.json().get(str(app_id), {})
                    if not data.get("success"):
                        return None
                    
                    app_data = data.get("data", {})
                    title = app_data.get("name", "")
                    price_overview = app_data.get("price_overview", {})
                    is_free = app_data.get("is_free", False)
                    
                    final_paise = price_overview.get("final", 0) if price_overview else 0
                    initial_paise = price_overview.get("initial", 0) if price_overview else 0
                    discount = price_overview.get("discount_percent", 0) if price_overview else 0
                    
                    price = float(final_paise) / 100.0 if not is_free else 0.0
                    original_price = float(initial_paise) / 100.0 if not is_free and initial_paise else price
                    
                    store_price = StorePriceInfo(
                        store=StoreType.STEAM,
                        store_item_id=str(app_id),
                        store_url=f"https://store.steampowered.com/app/{app_id}/",
                        price=price,
                        original_price=original_price,
                        discount_percent=discount,
                        currency="INR",
                        is_free=is_free,
                        in_stock=True,
                        formatted_price=f"₹{price:,.0f}" if not is_free else "Free",
                        formatted_original_price=f"₹{original_price:,.0f}" if original_price > 0 else "₹0"
                    )
                    
                    genres = [g.get("description", "") for g in app_data.get("genres", [])]
                    screenshots = [s.get("path_full", "") for s in app_data.get("screenshots", [])]
                    header_img = app_data.get("header_image", "")
                    
                    from app.services.normalizer import normalize_title
                    return GameDetails(
                        game_id=f"steam-{app_id}",
                        title=title,
                        normalized_title=normalize_title(title),
                        description=app_data.get("detailed_description", ""),
                        short_description=app_data.get("short_description", ""),
                        cover_image=header_img,
                        banner_image=header_img,
                        screenshots=screenshots[:5],
                        developer=", ".join(app_data.get("developers", [])),
                        publisher=", ".join(app_data.get("publishers", [])),
                        release_date=app_data.get("release_date", {}).get("date", ""),
                        genres=genres,
                        currency="INR",
                        cheapest_store=StoreType.STEAM,
                        stores=[store_price]
                    )
            except Exception as e:
                print(f"Error fetching Steam app details for {app_id}: {e}")
                return None
        return None

steam_service = SteamService()
