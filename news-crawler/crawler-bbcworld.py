import tweepy
from tweepy import OAuthHandler
import re
import urllib.request
from bs4 import BeautifulSoup

# twitter account info
consumer_key = 'JbMtJT7wEi0jks4prqI7deKLA'
consumer_secret = 'uf9csKWrKFoh2FVz8b25lx5fSygp9KLaGvU4SLMWPvJ12DTQeA'
access_token = '2922853605-LGXKANFWiDlhUTVGQg0WmPMKHg1AJmo5w5YINIu'
access_secret = 'fXdk8g5M7iBS9oJH58O8VaLDhj1S3xNEBMUjkH1njpFTl'

# authentication process
auth = OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_secret)

# creation object to query twitter
api = tweepy.API(auth)

# query to 1.handle newline and 2. handle multiple spaces
return_regexp = re.compile(r'[\n]')
space_regexp = re.compile(r'\s+')

# write results on file
with open('tweets.txt', 'w', encoding="utf8") as outfile:
    # tweets request
    bbc_tweets = api.user_timeline(screen_name ='BBCWorld', count=300)

    for tweet in bbc_tweets:

        # if it is not a retweet:
        if not 'RT' in tweet.text:

            # remove newlines form tweet text
            tweet_cleaned = return_regexp.sub('', tweet.text)

            print(tweet.id, tweet_cleaned)

            # extract URL from tweet
            tweet_url = re.search("(?P<url>https?://[^\s]+)", tweet_cleaned).group("url")

            # open URL and save the HTML
            page = urllib.request.urlopen(tweet_url)
            soup = BeautifulSoup(page, 'lxml')

            # find all HTML tag <p >
            story_par = soup.find_all('p')

            # flag that catch the end of the news
            text_end = bool(0)

            # here we store the text of the news
            story_text = ''

            # cycles all the p tags
            for idx, par in enumerate(story_par):

                # 11 is a bbc site related number
                if idx > 11 and not text_end:

                    # class that identifies the end of the news in bbc site
                    if par.attrs != {'class': ['top-stories-promo-story__summary', '']}:
                        story_text = story_text + par.text  # append the text

                    else:
                        text_end = bool(1)

            # delete newlines and multiple spaces
            story_text = return_regexp.sub(' ', story_text)
            story_text = space_regexp.sub(' ', story_text)

            # if the text is not empty
            if story_text:

                # if it is not a video-news
                if not 'Copy this link' in story_text:
                    outfile.write(str(tweet.id) + '\t' + str(tweet.created_at) + '\t' + tweet_url + '\t' +
                                  tweet_cleaned + '\t' + story_text + '\n')
