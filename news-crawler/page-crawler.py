import urllib.request
from bs4 import BeautifulSoup

# url = "https://en.wikipedia.org/wiki/List_of_state_and_union_territory_capitals_in_India"
# url = "https://www.nytimes.com/2017/12/12/world/europe/belgium-electricity.html?smid=tw-nytimes&smtyp=cur"
# url = "https://www.nytimes.com/2017/12/13/us/politics/tax-bill-republicans-deal.html?hp&action=click&pgtype=Homepage&clickSource=image&module=b-lede-package-region&region=top-news&WT.nav=top-news"
# url = "https://www.nytimes.com/interactive/2017/12/14/climate/republicans-global-warming-maps.html?hp&action=click&pgtype=Homepage&clickSource=story-heading&module=second-column-region&region=top-news&WT.nav=top-news"
# url = "https://t.co/dM34E1OHKU"
# url = "https://t.co/EqfoMZVZhb"
url = "https://t.co/6WKilAdtVz"

page = urllib.request.urlopen(url)

soup = BeautifulSoup(page, 'lxml')

# print(soup.prettify())

with open('page.txt', 'w', encoding="utf8") as outfile:
    outfile.write(soup.prettify())

all_par = soup.find_all('p')

story_par = soup.find_all('p')

text_end = bool(0)

for idx, par in enumerate(story_par):
    if idx > 11 and not text_end:
        if par.attrs != {'class': ['top-stories-promo-story__summary', '']}:
            print(par.text)
        else:
            text_end = bool(1)



# prendere prima riga di ogni file txt del testo della notizia così si evita il par sotto per l'help
# aggiungere campi nel file della news e magari mettere link + teto + caption twitter nella stessa riga
