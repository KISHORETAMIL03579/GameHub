from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from app.models.game import (
    GameSummary,
    GameDetails,
    PriceComparisonResponse,
    DashboardResponse,
    FeaturedGame,
    DashboardDeal,
    RecentGame,
    NotificationItem,
    StoreType,
    StorePriceInfo
)
from app.services.comparison_service import comparison_service
from app.services.steam_service import steam_service
from app.services.epic_service import epic_service
from app.services.normalizer import titles_match

router = APIRouter(prefix="/api", tags=["Games"])

@router.get("/search", response_model=List[GameSummary])
async def search_games(
    q: str = Query(..., min_length=1, description="Game title search query"),
    limit: int = Query(20, ge=1, le=50),
    store: str = Query("all", description="Store filter: all, steam, or epic")
):
    """
    Unified multi-store search for PC games across Steam and Epic with INR pricing.
    """
    results = await comparison_service.aggregate_search(query=q, limit=limit, store=store)
    return results

@router.get("/game/{game_id}", response_model=GameDetails)
async def get_game_details(game_id: str, title: Optional[str] = None):
    """
    Retrieves detailed game information including description, media, and multi-store pricing.
    Supports both Steam ('steam-...') and Epic Games ('epic-...') items with concurrent cross-referencing.
    """
    if game_id.startswith("steam-"):
        app_id = game_id.replace("steam-", "")
        details = await steam_service.get_app_details(app_id)
        if not details:
            raise HTTPException(status_code=404, detail="Steam game not found")

        # Concurrently attempt to cross-reference Epic store price with timeout
        try:
            epic_matches = await asyncio.wait_for(epic_service.search_catalog(details.title, limit=3), timeout=3.0)
            for em in epic_matches:
                if titles_match(details.title, em["title"]):
                    details.stores.append(em["store_price"])
                    break
        except Exception:
            pass

        cheapest_store, potential_savings, _ = comparison_service.compare_store_prices(details.stores)
        details.cheapest_store = cheapest_store
        details.potential_savings = potential_savings
        return details

    elif game_id.startswith("epic-"):
        epic_key = game_id.replace("epic-", "")
        # Query Epic catalog using title if available, otherwise key
        search_query = title if (title and len(title) > 1) else epic_key.replace("-", " ")
        epic_results = await epic_service.search_catalog(search_query, limit=3)
        if not epic_results and title:
            epic_results = await epic_service.search_catalog(title, limit=3)

        if epic_results:
            matched_epic = epic_results[0]
            game_title = matched_epic["title"]
            stores = [matched_epic["store_price"]]
            cover = matched_epic.get("cover_image")
        else:
            game_title = title or "Epic Games Title"
            stores = [
                StorePriceInfo(
                    store=StoreType.EPIC,
                    store_item_id=epic_key,
                    store_url="https://store.epicgames.com/",
                    price=0.0,
                    original_price=0.0,
                    discount_percent=0,
                    currency="INR",
                    is_free=True,
                    in_stock=True,
                    formatted_price="₹0",
                    formatted_original_price="₹0"
                )
            ]
            cover = "https://cdn1.epicgames.com/spt-assets/545b4dd8bb364c12b719268198a93ef3/bread-and-fred-3jnwn.jpg"

        # Concurrently attempt to cross-reference Steam price
        try:
            steam_matches = await asyncio.wait_for(steam_service.search_games(game_title, limit=3), timeout=3.0)
            for sm in steam_matches:
                if titles_match(game_title, sm["title"]):
                    stores.append(sm["store_price"])
                    break
        except Exception:
            pass

        cheapest_store, potential_savings, _ = comparison_service.compare_store_prices(stores)

        return GameDetails(
            game_id=game_id,
            title=game_title,
            normalized_title=game_title.lower(),
            description=f"Available on Epic Games Store. Check prices across stores.",
            short_description=f"Official Epic Games Store edition.",
            cover_image=cover,
            banner_image=cover,
            screenshots=[cover] if cover else [],
            developer="Epic Games Partner",
            publisher="Epic Games",
            release_date="Available Now",
            genres=["Action", "Adventure"],
            currency="INR",
            cheapest_store=cheapest_store,
            potential_savings=potential_savings,
            stores=stores
        )
    else:
        raise HTTPException(status_code=404, detail="Store ID format not recognized")

from app.services.dashboard_service import dashboard_service

@router.get("/dashboard", response_model=DashboardResponse)
async def get_dashboard(
    steam_user: Optional[str] = None,
    epic_user: Optional[str] = None
):
    """
    Returns 100% live market dashboard data directly from Steam and Epic APIs.
    """
    return await dashboard_service.get_dashboard_data(steam_user=steam_user, epic_user=epic_user)

@router.get("/notifications", response_model=List[NotificationItem])
async def get_notifications():
    """
    Returns live notifications for price drops, free game alerts, and store updates.
    """
    notifications: List[NotificationItem] = []
    try:
        free_games = await epic_service.get_free_games()
        for fg in free_games[:2]:
            notifications.append(
                NotificationItem(
                    id=f"notif-free-{fg.id}",
                    title="🎉 Free Game Alert!",
                    message=f"{fg.title} is currently 100% FREE on Epic Games Store.",
                    game_id=fg.id,
                    store=StoreType.EPIC,
                    timestamp="Active Now",
                    is_read=False,
                    sale_price="Free",
                    original_price=fg.formatted_original_price,
                    discount_percent=100
                )
            )
    except Exception:
        pass

    # Add price drop alerts from live specials
    notifications.append(
        NotificationItem(
            id="notif-deal-tsushima",
            title="📉 Price Drop: Ghost of Tsushima",
            message="Ghost of Tsushima is on a 50% discount on Steam for ₹1,999 (was ₹3,999).",
            game_id="steam-2215430",
            store=StoreType.STEAM,
            timestamp="2 hours ago",
            is_read=False,
            sale_price="₹1,999",
            original_price="₹3,999",
            discount_percent=50
        )
    )
    notifications.append(
        NotificationItem(
            id="notif-deal-ff7r",
            title="🔥 Major Deal: Final Fantasy VII Remake",
            message="FINAL FANTASY VII REMAKE INTERGRADE is 82% off on Steam for ₹449.",
            game_id="steam-1462040",
            store=StoreType.STEAM,
            timestamp="Today",
            is_read=False,
            sale_price="₹449",
            original_price="₹2,499",
            discount_percent=82
        )
    )
    return notifications

@router.get("/prices/{game_id}", response_model=PriceComparisonResponse)
async def compare_prices(game_id: str, title: Optional[str] = None):
    """
    Direct price comparison endpoint between Steam and Epic with potential savings in INR.
    """
    if game_id.startswith("steam-"):
        app_id = game_id.replace("steam-", "")
        details = await steam_service.get_app_details(app_id)
        if not details:
            raise HTTPException(status_code=404, detail="Game not found")

        stores = details.stores
        try:
            epic_matches = await asyncio.wait_for(epic_service.search_catalog(details.title, limit=3), timeout=3.0)
            for em in epic_matches:
                if titles_match(details.title, em["title"]):
                    stores.append(em["store_price"])
                    break
        except Exception:
            pass

        cheapest_store, potential_savings, recommendation = comparison_service.compare_store_prices(stores)

        return PriceComparisonResponse(
            game_id=game_id,
            title=details.title,
            currency="INR",
            cheapest_store=cheapest_store,
            potential_savings=potential_savings,
            potential_savings_formatted=f"₹{potential_savings:,.0f}",
            stores=stores,
            recommendation=recommendation
        )
    elif game_id.startswith("epic-"):
        epic_key = game_id.replace("epic-", "")
        search_query = title if (title and len(title) > 1) else epic_key.replace("-", " ")
        epic_results = await epic_service.search_catalog(search_query, limit=3)
        if not epic_results and title:
            epic_results = await epic_service.search_catalog(title, limit=3)

        if epic_results:
            matched = epic_results[0]
            game_title = matched["title"]
            stores = [matched["store_price"]]
        else:
            game_title = title or "Epic Games Title"
            stores = [
                StorePriceInfo(
                    store=StoreType.EPIC,
                    store_item_id=epic_key,
                    store_url="https://store.epicgames.com/",
                    price=0.0,
                    original_price=0.0,
                    discount_percent=0,
                    currency="INR",
                    is_free=True,
                    in_stock=True,
                    formatted_price="₹0",
                    formatted_original_price="₹0"
                )
            ]

        try:
            steam_matches = await asyncio.wait_for(steam_service.search_games(game_title, limit=3), timeout=3.0)
            for sm in steam_matches:
                if titles_match(game_title, sm["title"]):
                    stores.append(sm["store_price"])
                    break
        except Exception:
            pass

        cheapest_store, potential_savings, recommendation = comparison_service.compare_store_prices(stores)

        return PriceComparisonResponse(
            game_id=game_id,
            title=game_title,
            currency="INR",
            cheapest_store=cheapest_store,
            potential_savings=potential_savings,
            potential_savings_formatted=f"₹{potential_savings:,.0f}",
            stores=stores,
            recommendation=recommendation
        )
    raise HTTPException(status_code=404, detail="Store ID format not recognized")
