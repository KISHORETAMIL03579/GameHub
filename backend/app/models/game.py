from typing import List, Optional
from pydantic import BaseModel, Field
from enum import Enum

class StoreType(str, Enum):
    STEAM = "Steam"
    EPIC = "Epic"
    UNKNOWN = "Unknown"

class StorePriceInfo(BaseModel):
    store: StoreType
    store_item_id: str
    store_url: str
    price: float = Field(..., description="Current price in INR (0.0 if free)")
    original_price: float = Field(..., description="Original price before discount in INR")
    discount_percent: int = Field(default=0, description="Discount percentage (0-100)")
    currency: str = "INR"
    is_free: bool = False
    in_stock: bool = True
    formatted_price: str = "₹0"
    formatted_original_price: str = "₹0"

class GameSummary(BaseModel):
    game_id: str
    title: str
    cover_image: Optional[str] = None
    cheapest_store: Optional[StoreType] = None
    lowest_price: Optional[float] = None
    original_price: Optional[float] = None
    highest_discount_percent: int = 0
    formatted_lowest_price: Optional[str] = None
    stores: List[StorePriceInfo] = []

class GameDetails(BaseModel):
    game_id: str
    title: str
    normalized_title: str
    description: Optional[str] = None
    short_description: Optional[str] = None
    cover_image: Optional[str] = None
    banner_image: Optional[str] = None
    screenshots: List[str] = []
    developer: Optional[str] = None
    publisher: Optional[str] = None
    release_date: Optional[str] = None
    genres: List[str] = []
    currency: str = "INR"
    cheapest_store: Optional[StoreType] = None
    potential_savings: float = 0.0
    stores: List[StorePriceInfo] = []

class PriceComparisonResponse(BaseModel):
    game_id: str
    title: str
    currency: str = "INR"
    cheapest_store: Optional[StoreType] = None
    potential_savings: float = 0.0
    potential_savings_formatted: str = "₹0"
    stores: List[StorePriceInfo] = []
    recommendation: Optional[str] = None

class FreeGameOffer(BaseModel):
    id: str
    title: str
    store: StoreType
    cover_image: Optional[str] = None
    store_url: str
    original_price: float = 0.0
    formatted_original_price: str = "₹0"
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    is_active: bool = True

class LibraryGame(BaseModel):
    id: str
    title: str
    store: StoreType
    store_item_id: str
    cover_image: Optional[str] = None
    playtime_minutes: int = 0
    playtime_hours: float = 0.0
    last_played: Optional[str] = None
    estimated_value_inr: float = 0.0

class UnifiedLibraryResponse(BaseModel):
    total_games: int = 0
    steam_count: int = 0
    epic_count: int = 0
    duplicate_count: int = 0
    estimated_total_value_inr: float = 0.0
    total_playtime_hours: float = 0.0
    duplicates: List[str] = []
    games: List[LibraryGame] = []

class ConnectAccountRequest(BaseModel):
    account_identifier: str = Field(..., description="SteamID64 or Epic Account Email/ID")
    display_name: Optional[str] = None

class ConnectAccountResponse(BaseModel):
    success: bool
    store: StoreType
    account_identifier: str
    display_name: str
    games_imported: int = 0
    message: str

class FeaturedGame(BaseModel):
    game_id: str
    title: str
    subtitle: str
    banner_image: str
    lowest_price: float
    formatted_lowest_price: str
    cheapest_store: StoreType
    discount_percent: int = 0
    stores: List[StorePriceInfo] = []

class DashboardDeal(BaseModel):
    game_id: str
    title: str
    cover_image: str
    store: StoreType
    sale_price: float
    original_price: float
    formatted_sale_price: str
    discount_percent: int

class RecentGame(BaseModel):
    game_id: str
    title: str
    cover_image: str

class NotificationItem(BaseModel):
    id: str
    title: str
    message: str
    game_id: Optional[str] = None
    store: Optional[StoreType] = None
    timestamp: str = "Just now"
    is_read: bool = False
    sale_price: Optional[str] = None
    original_price: Optional[str] = None
    discount_percent: int = 0

class DashboardResponse(BaseModel):
    steam_count: int = 124
    epic_count: int = 56
    featured_game: FeaturedGame
    best_deals: List[DashboardDeal]
    recently_added: List[RecentGame]
