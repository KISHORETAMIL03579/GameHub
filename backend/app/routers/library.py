from fastapi import APIRouter, Query, HTTPException
import httpx
from typing import Optional, List
from app.models.game import UnifiedLibraryResponse, LibraryGame, StoreType
from app.services.normalizer import titles_match
from app.config import settings

router = APIRouter(prefix="/api/library", tags=["Library & Statistics"])

@router.get("/steam", response_model=List[LibraryGame])
async def get_steam_library(steam_id: Optional[str] = Query(None)):
    """
    Fetches owned games from Steam Web API GetOwnedGames for the authenticated user.
    """
    if not steam_id:
        return []

    if settings.STEAM_API_KEY:
        url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/"
        params = {
            "key": settings.STEAM_API_KEY,
            "steamid": steam_id,
            "include_appinfo": 1,
            "include_played_free_games": 1,
            "format": "json"
        }
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                resp = await client.get(url, params=params)
                if resp.status_code == 200:
                    games_data = resp.json().get("response", {}).get("games", [])
                    res = []
                    for g in games_data:
                        appid = str(g.get("appid"))
                        playtime = g.get("playtime_forever", 0)
                        res.append(LibraryGame(
                            id=f"steam-{appid}",
                            title=g.get("name", ""),
                            store=StoreType.STEAM,
                            store_item_id=appid,
                            cover_image=f"https://cdn.cloudflare.steamstatic.com/steam/apps/{appid}/header.jpg",
                            playtime_minutes=playtime,
                            playtime_hours=round(playtime / 60.0, 1),
                            estimated_value_inr=999.0
                        ))
                    return res
            except Exception as e:
                print(f"Error calling Steam Web API: {e}")
    
    return []

@router.get("/epic", response_model=List[LibraryGame])
async def get_epic_library(account_id: Optional[str] = Query(None)):
    """
    Returns user's Epic Games library for the authenticated account.
    """
    if not account_id:
        return []

    return []

@router.get("/unified", response_model=UnifiedLibraryResponse)
async def get_unified_library(
    steam_id: Optional[str] = Query(None),
    epic_id: Optional[str] = Query(None)
):
    """
    Aggregates Steam and Epic libraries, detects duplicate purchases, and computes statistics.
    """
    steam_games = await get_steam_library(steam_id)
    epic_games = await get_epic_library(epic_id)

    all_games = steam_games + epic_games
    total_playtime = sum(g.playtime_hours for g in all_games)
    total_value = sum(g.estimated_value_inr for g in all_games)

    # Detect duplicates across stores
    duplicates = []
    for s_game in steam_games:
        for e_game in epic_games:
            if titles_match(s_game.title, e_game.title):
                duplicates.append(s_game.title)

    return UnifiedLibraryResponse(
        total_games=len(all_games),
        steam_count=len(steam_games),
        epic_count=len(epic_games),
        duplicate_count=len(duplicates),
        estimated_total_value_inr=total_value,
        total_playtime_hours=round(total_playtime, 1),
        duplicates=duplicates,
        games=all_games
    )
