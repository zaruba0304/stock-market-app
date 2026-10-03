with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

import re

# Test the regex
pattern = r'&(?!amp;|#|[a-zA-Z])'
matches = list(re.finditer(pattern, content))
print(f'Regex matches: {len(matches)}')

# Test replacement with a different string to see if it works
def replace_func(match):
    return 'REPLACED'

new_content = re.sub(pattern, replace_func, content)
print(f'Replacements made: {new_content != content}')

# Check the result
for m in re.finditer(pattern, new_content):
    start = max(0, m.start()-10)
    end = min(len(new_content), m.end()+10)
    print(f'  Still matches at {m.start()}: {repr(new_content[start:end])}')

# Check specific strings
import re
for match in re.finditer(r'<string name="track_apply_ipos">(.*?)</string>', new_content):
    print(f'track_apply_ipos: {repr(match.group(1))}')
for match in re.finditer(r'<string name="news_analysis">(.*?)</string>', new_content):
    print(f'news_analysis: {repr(match.group(1))}')
for match in re.finditer(r'<string name="settings_help">(.*?)</string>', new_content):
    print(f'settings_help: {repr(match.group(1))}')