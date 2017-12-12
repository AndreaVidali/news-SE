import tweepy
from tweepy import OAuthHandler
import re

consumer_key = 'JbMtJT7wEi0jks4prqI7deKLA'
consumer_secret = 'uf9csKWrKFoh2FVz8b25lx5fSygp9KLaGvU4SLMWPvJ12DTQeA'
access_token = '2922853605-LGXKANFWiDlhUTVGQg0WmPMKHg1AJmo5w5YINIu'
access_secret = 'fXdk8g5M7iBS9oJH58O8VaLDhj1S3xNEBMUjkH1njpFTl'

auth = OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_secret)

api = tweepy.API(auth)

mentions_regexp = re.compile(r'@[^\s]*')
url_regexp = re.compile(r'htt[^\s]*')
return_regexp = re.compile(r'[\n]')
rt_regexp = re.compile(r'RT ')
spacestart_regexp = re.compile(r'^ +')

with open('docs.txt', 'w', encoding="utf8") as outfile:
    bbc_tweets = api.user_timeline(screen_name='bbcworld', count=300)
    for tweet in bbc_tweets:
        print(tweet.created_at, tweet.text, len(tweet.text))
        tweet_text = mentions_regexp.sub('', tweet.text)
        tweet_text = url_regexp.sub('', tweet_text)
        tweet_text = return_regexp.sub('', tweet_text)
        tweet_text = rt_regexp.sub('', tweet_text)
        tweet_text = spacestart_regexp.sub('', tweet_text)

        # print('Tweet cleaned: ' + tweet.text)
        outfile.write(str(tweet.id) + '\t' + str(tweet.created_at) + '\t' + tweet_text + '\n')
