with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

import re

# Replace all & that are not part of an entity reference
# Use a function to return the proper replacement
def replace_ampersands(match):
    return '&'

# Use a more precise regex: & not followed by amp; or #x? or letters
content = re.sub(r'&(?!amp;|#|[a-zA-Z])', replace_ampersands, content)

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed all ampersands using regex with function')