with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()
import re
for match in re.finditer(r'<string name="track_apply_ipos">(.*?)</string>', content):
    print('track_apply_ipos:', repr(match.group(1)))
for match in re.finditer(r'<string name="news_analysis">(.*?)</string>', content):
    print('news_analysis:', repr(match.group(1)))
for match in re.finditer(r'<string name="settings_help">(.*?)</string>', content):
    print('settings_help:', repr(match.group(1)))