with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# Check the exact bytes around the ampersands
import re
for match in re.finditer(r'Track . Apply IPOs', content):
    start = max(0, match.start()-10)
    end = min(len(content), match.end()+10)
    print('track_apply_ipos bytes:', [hex(ord(c)) for c in content[start:end]])

for match in re.finditer(r'News . Analysis', content):
    start = max(0, match.start()-10)
    end = min(len(content), match.end()+10)
    print('news_analysis bytes:', [hex(ord(c)) for c in content[start:end]])

for match in re.finditer(r'Help . Support', content):
    start = max(0, match.start()-10)
    end = min(len(content), match.end()+10)
    print('settings_help bytes:', [hex(ord(c)) for c in content[start:end]])