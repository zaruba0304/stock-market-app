with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

import re

# Test the regex
pattern = r'&(?!amp;|#|[a-zA-Z])'

# Replace with the literal string "&" (5 characters)
# In Python, we need to use the actual characters
replacement = '&' + 'amp;'
print(f'Replacement string: {repr(replacement)}')

new_content = re.sub(pattern, replacement, content)
print(f'Replacements made: {new_content != content}')

# Check specific strings
for match in re.finditer(r'<string name="track_apply_ipos">(.*?)</string>', new_content):
    print(f'track_apply_ipos: {repr(match.group(1))}')
for match in re.finditer(r'<string name="news_analysis">(.*?)</string>', new_content):
    print(f'news_analysis: {repr(match.group(1))}')
for match in re.finditer(r'<string name="settings_help">(.*?)</string>', new_content):
    print(f'settings_help: {repr(match.group(1))}')

# Check for remaining unescaped ampersands
matches = list(re.finditer(r'&(?!amp;|#|[a-zA-Z])', new_content))
print(f'Remaining unescaped ampersands: {len(matches)}')

# Write the fixed content
with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(new_content)

print('Fixed and saved')