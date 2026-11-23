import re

with open(r"d:\Projects\Android\android\build\reports\problems\problems-report.html", "r", encoding="utf-8") as f:
    html = f.read()

# Extract script tag content or json strings
for m in re.findall(r'"(warning|deprecated|deprecation|message|description)":\s*"([^"]+)"', html, re.IGNORECASE):
    print(m)

# Find any strings mentioning deprecation
deprecations = set(re.findall(r'(\b[A-Za-z0-9_.]+\b is deprecated[^\.\"]*)', html))
print("\nDeprecations found:")
for d in deprecations:
    print("-", d)
