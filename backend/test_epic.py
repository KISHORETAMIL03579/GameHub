import asyncio
import httpx

async def test():
    url = "https://graphql.epicgames.com/graphql"
    query = """
    query searchStoreQuery(
        $allowCountries: String, 
        $category: String, 
        $count: Int, 
        $country: String!, 
        $keywords: String, 
        $locale: String, 
        $namespace: String, 
        $sortBy: String, 
        $sortDir: String, 
        $start: Int
    ) {
        Catalog {
            searchStore(
                allowCountries: $allowCountries, 
                category: $category, 
                count: $count, 
                country: $country, 
                keywords: $keywords, 
                locale: $locale, 
                namespace: $namespace, 
                sortBy: $sortBy, 
                sortDir: $sortDir, 
                start: $start
            ) {
                elements {
                    title
                    id
                    namespace
                    description
                    productSlug
                    urlSlug
                    keyImages {
                        type
                        url
                    }
                    price(country: $country) {
                        totalPrice {
                            originalPrice
                            discountPrice
                            currencyCode
                            fmtPrice {
                                originalPrice
                                discountPrice
                            }
                        }
                    }
                }
            }
        }
    }
    """
    variables = {
        "allowCountries": "IN",
        "category": "games/edition/base|games|bundles",
        "count": 10,
        "country": "IN",
        "keywords": "Cyberpunk",
        "locale": "en-US",
        "sortBy": "relevance",
        "sortDir": "DESC",
        "start": 0
    }
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Content-Type": "application/json"
    }
    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.post(url, json={"query": query, "variables": variables}, headers=headers)
        print("Status:", resp.status_code)
        if resp.status_code == 200:
            data = resp.json()
            elems = data.get("data", {}).get("Catalog", {}).get("searchStore", {}).get("elements", [])
            print(f"Elements returned: {len(elems)}")
            for e in elems:
                price = e.get("price", {}).get("totalPrice", {})
                fmt = price.get("fmtPrice", {})
                print(f" - {e.get('title')}: fmt={fmt.get('discountPrice')} (raw: {price.get('discountPrice')} {price.get('currencyCode')})")
        else:
            print(resp.text[:300])

if __name__ == "__main__":
    asyncio.run(test())
