from fastapi import APIRouter
from typing import List
from app.models.game import FreeGameOffer
from app.services.epic_service import epic_service

router = APIRouter(prefix="/api", tags=["Offers & Deals"])

@router.get("/free-games", response_model=List[FreeGameOffer])
async def get_free_games():
    """
    Returns currently active and upcoming free PC games from the Epic Games Store with expiry timers.
    """
    return await epic_service.get_free_games()

@router.get("/offers", response_model=List[FreeGameOffer])
async def get_current_offers():
    """
    Returns highlighted promotional offers across stores.
    """
    return await epic_service.get_free_games()
