with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace all unescaped ampersands
content = content.replace('Track & Apply IPOs', 'Track & Apply IPOs')
content = content.replace('News & Analysis', 'News & Analysis')
content = content.replace('Help & Support', 'Help & Support')

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed all ampersands')