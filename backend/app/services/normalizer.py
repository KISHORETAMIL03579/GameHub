import re

def normalize_title(title: str) -> str:
    """
    Normalizes a game title for cross-store matching.
    Removes edition suffixes, trademarks, punctuation, and extra whitespace.
    """
    if not title:
        return ""
    
    # Lowercase
    clean = title.lower()

    # Remove trademark, copyright, registered symbols
    clean = re.sub(r'[\u2122\u00ae\u00a9]', '', clean)

    # Remove common edition & bundle suffixes
    clean = re.sub(r'(\b(standard|deluxe|definitive|goty|game of the year|enhanced|remastered|special|gold|ultimate|premium|anniversary|director\'s cut)\s+edition\b)', '', clean)
    clean = re.sub(r'(\b(edition|bundle|collection|remastered|definitive)\b)', '', clean)

    # Replace roman numerals in common sequel contexts: ' ii' -> ' 2', ' iii' -> ' 3', ' iv' -> ' 4', ' v' -> ' 5'
    roman_map = [
        (r'\bviii\b', '8'),
        (r'\bvii\b', '7'),
        (r'\bvi\b', '6'),
        (r'\biv\b', '4'),
        (r'\bv\b', '5'),
        (r'\biii\b', '3'),
        (r'\bii\b', '2'),
        (r'\bi\b', '1')
    ]
    for pattern, rep in roman_map:
        clean = re.sub(pattern, rep, clean)

    # Remove non-alphanumeric characters (keep numbers and letters)
    clean = re.sub(r'[^a-z0-9\s]', ' ', clean)

    # Collapse multiple whitespaces
    clean = re.sub(r'\s+', ' ', clean).strip()

    return clean

def titles_match(title1: str, title2: str) -> bool:
    """
    Checks whether two game titles refer to the same base game.
    """
    norm1 = normalize_title(title1)
    norm2 = normalize_title(title2)
    
    if not norm1 or not norm2:
        return False
        
    if norm1 == norm2:
        return True
        
    # Check if one is a clean prefix or close substring
    if norm1 in norm2 or norm2 in norm1:
        len_ratio = min(len(norm1), len(norm2)) / max(len(norm1), len(norm2))
        return len_ratio >= 0.75
        
    return False
