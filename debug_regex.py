with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

import re

# Test the regex
pattern = r'&(?!amp;|#|[a-zA-Z])'
matches = list(re.finditer(pattern, content))
print(f'Regex matches: {len(matches)}')
for m in matches:
    start = max(0, m.start()-10)
    end = min(len(content), m.end()+10)
    print(f'  Match at {m.start()}: {repr(content[start:end])}')

# Test replacement
def replace_func(match):
    print(f'  Replacing at {match.start()}: {repr(match.group())}')
    return '&'

new_content = re.sub(pattern, replace_func, content)
print(f'Replacements made: {new_content != content}')

# Check if content changed
if new_content != content:
    print('Content changed!')
    with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
        f.write(new_content)
else:
    print('Content NOT changed')