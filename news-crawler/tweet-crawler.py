import tweepy
from tweepy import OAuthHandler
import re

import urllib.request
from bs4 import BeautifulSoup

consumer_key = 'JbMtJT7wEi0jks4prqI7deKLA'
consumer_secret = 'uf9csKWrKFoh2FVz8b25lx5fSygp9KLaGvU4SLMWPvJ12DTQeA'
access_token = '2922853605-LGXKANFWiDlhUTVGQg0WmPMKHg1AJmo5w5YINIu'
access_secret = 'fXdk8g5M7iBS9oJH58O8VaLDhj1S3xNEBMUjkH1njpFTl'

auth = OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_secret)

api = tweepy.API(auth)

return_regexp = re.compile(r'[\n]')
space_regexp = re.compile(r'\s+')

with open('tweets.txt', 'w', encoding="utf8") as outfile:
    bbc_tweets = api.user_timeline(screen_name='BBCWorld', count=300)
    for tweet in bbc_tweets:
        if not 'RT' in tweet.text:

            tweet_cleaned = return_regexp.sub('', tweet.text)
            print(tweet.id, tweet_cleaned)

            # try:
            tweet_url = re.search("(?P<url>https?://[^\s]+)", tweet_cleaned).group("url")
            # except:
            #    tweet_url = 'none'

            page = urllib.request.urlopen(tweet_url)
            soup = BeautifulSoup(page, 'lxml')

            story_par = soup.find_all('p')

            text_end = bool(0)
            story_text = ''

            for idx, par in enumerate(story_par):
                if idx > 11 and not text_end:
                    if par.attrs != {'class': ['top-stories-promo-story__summary', '']}:
                        story_text = story_text + par.text
                    else:
                        text_end = bool(1)

            story_text = return_regexp.sub(' ', story_text)
            story_text = space_regexp.sub(' ', story_text)

            if story_text:
                if not 'Copy this link' in story_text:
                    outfile.write(str(tweet.id) + '\t' + str(tweet.created_at) + '\t' + tweet_url + '\t' +
                                  tweet_cleaned + '\t' + story_text + '\n')
