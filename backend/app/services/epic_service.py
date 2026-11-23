import asyncio
from typing import List, Optional, Dict, Any
import httpx
from epicstore_api import EpicGamesStoreAPI
from app.config import settings
from app.models.game import StorePriceInfo, StoreType, FreeGameOffer
from app.services.normalizer import normalize_title

class EpicService:
    def __init__(self):
        self.free_games_url = settings.EPIC_FREE_GAMES_URL
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
        self._egs_api = EpicGamesStoreAPI(country=settings.DEFAULT_COUNTRY_CODE, locale="en-US")

    async def get_free_games(self) -> List[FreeGameOffer]:
        """
        Fetches the current and upcoming free games on the Epic Games Store with INR original pricing.
        """
        params = {
            "locale": "en-US",
            "country": settings.DEFAULT_COUNTRY_CODE,
            "allowCountries": settings.DEFAULT_COUNTRY_CODE
        }
        
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                resp = await client.get(self.free_games_url, params=params, headers=self.headers)
                if resp.status_code == 200:
                    data = resp.json()
                    elements = data.get("data", {}).get("Catalog", {}).get("searchStore", {}).get("elements", [])
                    free_offers = []
                    
                    for el in elements:
                        promotions = el.get("promotions") or {}
                        promo_offers = promotions.get("promotionalOffers", [])
                        upcoming_promos = promotions.get("upcomingPromotionalOffers", [])
                        
                        is_active_free = False
                        start_date = None
                        end_date = None
                        
                        # Active offers
                        if promo_offers:
                            for po in promo_offers:
                                for offer in po.get("promotionalOffers", []):
                                    discount_setting = offer.get("discountSetting", {})
                                    if discount_setting.get("discountPercentage") == 0:
                                        is_active_free = True
                                        start_date = offer.get("startDate")
                                        end_date = offer.get("endDate")
                                        break
                        
                        # Upcoming offers
                        if not is_active_free and upcoming_promos:
                            for po in upcoming_promos:
                                for offer in po.get("promotionalOffers", []):
                                    discount_setting = offer.get("discountSetting", {})
                                    if discount_setting.get("discountPercentage") == 0:
                                        start_date = offer.get("startDate")
                                        end_date = offer.get("endDate")
                                        break

                        # Extract price in INR
                        price_data = el.get("price", {}).get("totalPrice", {})
                        orig_val = price_data.get("originalPrice", 0) or 0
                        orig_price_inr = float(orig_val) / 100.0 if orig_val > 500 else float(orig_val)

                        # Images
                        key_images = el.get("keyImages", [])
                        cover_image = None
                        for img in key_images:
                            if img.get("type") in ["Thumbnail", "DieselStoreFrontWide", "OfferImageWide"]:
                                cover_image = img.get("url")
                                break
                        if not cover_image and key_images:
                            cover_image = key_images[0].get("url")

                        title = el.get("title", "")
                        slug = el.get("productSlug") or el.get("urlSlug") or ""
                        store_url = f"https://store.epicgames.com/p/{slug}" if slug else "https://store.epicgames.com"

                        if is_active_free or upcoming_promos:
                            free_offers.append(FreeGameOffer(
                                id=f"epic-{el.get('id', slug)}",
                                title=title,
                                store=StoreType.EPIC,
                                cover_image=cover_image,
                                store_url=store_url,
                                original_price=orig_price_inr,
                                formatted_original_price=f"₹{orig_price_inr:,.0f}" if orig_price_inr > 0 else "₹0",
                                start_date=start_date,
                                end_date=end_date,
                                is_active=is_active_free
                            ))
                    return free_offers
            except Exception as e:
                print(f"Error querying Epic free games: {e}")
                return []
        return []

    async def search_catalog(self, query: str, limit: int = 15) -> List[Dict[str, Any]]:
        """
        Queries Epic Games store catalog using Epic Storefront GraphQL for real INR pricing.
        """
        norm_query = normalize_title(query)
        if not norm_query:
            return []

        try:
            # Run the synchronous cloudscraper-backed fetch in threadpool
            res = await asyncio.to_thread(
                self._egs_api.fetch_store_games,
                keywords=query,
                count=limit,
                allow_countries=settings.DEFAULT_COUNTRY_CODE
            )

            elements = res.get("data", {}).get("Catalog", {}).get("searchStore", {}).get("elements", [])
            results = []

            for el in elements:
                title = el.get("title", "")
                if not title:
                    continue

                price_data = el.get("price", {}).get("totalPrice", {})
                fmt_price = price_data.get("fmtPrice", {})
                
                orig_val = price_data.get("originalPrice", 0) or 0
                disc_val = price_data.get("discountPrice", 0) or 0

                # Currency info (decimals typically 2: 249900 = ₹2,499.00)
                orig_price = float(orig_val) / 100.0 if orig_val > 500 else float(orig_val)
                discount_price = float(disc_val) / 100.0 if disc_val > 500 else float(disc_val)

                is_free = discount_price == 0 and disc_val == 0
                discount_pct = 0
                if orig_price > 0 and discount_price < orig_price:
                    discount_pct = int(round((orig_price - discount_price) / orig_price * 100))

                key_images = el.get("keyImages", [])
                cover_image = None
                for img in key_images:
                    if img.get("type") in ["Thumbnail", "DieselStoreFrontWide", "OfferImageWide"]:
                        cover_image = img.get("url")
                        break
                if not cover_image and key_images:
                    cover_image = key_images[0].get("url")

                slug = el.get("productSlug") or el.get("urlSlug") or ""
                store_url = f"https://store.epicgames.com/p/{slug}" if slug else "https://store.epicgames.com"

                formatted_discount = fmt_price.get("discountPrice") or (f"₹{discount_price:,.0f}" if not is_free else "Free")
                formatted_original = fmt_price.get("originalPrice") or (f"₹{orig_price:,.0f}" if orig_price > 0 else "₹0")

                store_price = StorePriceInfo(
                    store=StoreType.EPIC,
                    store_item_id=str(el.get("id", slug)),
                    store_url=store_url,
                    price=discount_price,
                    original_price=orig_price,
                    discount_percent=discount_pct,
                    currency="INR",
                    is_free=is_free,
                    in_stock=True,
                    formatted_price=formatted_discount if not is_free else "Free",
                    formatted_original_price=formatted_original
                )

                results.append({
                    "id": f"epic-{el.get('id', slug)}",
                    "title": title,
                    "cover_image": cover_image,
                    "store_price": store_price
                })

            return results
        except Exception as e:
            print(f"Error querying Epic catalog search: {e}")
            return []

epic_service = EpicService()
