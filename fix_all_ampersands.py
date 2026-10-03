with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

import re

# Replace all & that are not part of an entity reference
# This regex matches & that is not followed by amp; or # or letters
def replace_ampersands(match):
    return '&'

# Use a more precise regex: & not followed by amp; or #x? or letters
content = re.sub(r'&(?!amp;|#|[a-zA-Z])', '&', content)

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed all ampersands using regex')