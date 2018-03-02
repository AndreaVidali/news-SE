import tweepy
from tweepy import OAuthHandler
import re
import csv
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

# tweets request
# bbc_tweets = api.user_timeline(screen_name='BBCWorld', count=15)

# write results on file
with open('news-bbcworld.txt', 'a', encoding="utf8") as outfile:

    for tweet in tweepy.Cursor(api.user_timeline, screen_name='BBCWorld').items():

        # remove newlines form tweet text and print
        tweet_cleaned = return_regexp.sub('', tweet.text)
        print(tweet.id, tweet_cleaned)

        # if it's not a retweet:
        if not 'RT' in tweet.text:

            # check if the tweet it has been downloaded yet
            tweet_present = False

            with open("news-bbcworld.txt", encoding="utf8") as tweets_file:
                for line in csv.reader(tweets_file, dialect="excel-tab"):
                    if str(tweet.id) == line[0]:
                        tweet_present = True
                        print('--- Gia presente')

            if not tweet_present:

                # extract URL from tweet
                tweet_url = re.search("(?P<url>https?://[^\s]+)", tweet_cleaned).group("url")

                try:
                    # open URL and save the HTML
                    page = urllib.request.urlopen(tweet_url)
                    soup = BeautifulSoup(page, 'lxml')

                    # find all HTML tag <p >
                    story_par = soup.find_all('p')

                    # here we store the text of the news
                    story_text = ''

                    # flag that catch the begin and the end of the news
                    text_begin = False
                    text_end = False

                    for par in story_par:
                        if par.attrs == {'class': ['story-body__introduction']}:
                            text_begin = True
                        if par.attrs == {'class': ['top-stories-promo-story__summary', '']}:
                            text_end = True
                        if text_begin and not text_end:
                            story_text = story_text + ' ' + par.text  # append the text

                    # delete newlines and multiple spaces
                    story_text = return_regexp.sub(' ', story_text)
                    story_text = space_regexp.sub(' ', story_text)

                    # if the text is not empty
                    if story_text:

                        # if it is not a video-news
                        if not 'Copy this link' in story_text:
                            outfile.write(str(tweet.id) + '\t' + str(tweet.created_at) + '\t' + tweet_url + '\t' +
                                          tweet_cleaned + '\t' + story_text + '\n')
                            print('--- Done!')
                        else:
                            print('--- Error: copy this link')
                    else:
                        print('--- Nessun testo estratto')

                except:
                    print('URL not valid')
        else:
            print('--- Retweet')
