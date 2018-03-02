import urllib.request
from bs4 import BeautifulSoup

url = "https://www.reuters.com/article/us-usa-trade/trump-says-u-s-to-impose-tariffs-on-steel-aluminum-imports-idUSKCN1GD3QO?utm_campaign=trueAnthem:+Trending+Content&utm_content=5a989b0e04d3013d0ea997be&utm_medium=trueAnthem&utm_source=twitter"

page = urllib.request.urlopen(url)

soup = BeautifulSoup(page, 'lxml')

# print(soup.prettify())

with open('page.txt', 'w', encoding="utf8") as outfile:
    outfile.write(soup.prettify())


story_par = soup.find_all('p')

'''
for idx, par in enumerate(story_par):
    if idx > 11 and not text_end:
        if par.attrs != {'class': ['top-stories-promo-story__summary', '']}:
            print(par.text)
        else:
            text_end = bool(1)
'''

text_begin = False
text_end = False


for par in story_par:
    if par.attrs == {'class': ['MegaArticleBody_first-p_2htdt']}:
    #     text_begin = True
    # if par.attrs == {'class': ['top-stories-promo-story__summary', '']}:
    #     text_end = True
    # if text_begin and not text_end:
        print(par.text)
    if par.attrs == {'class': ['']}:
        print(par.text)

