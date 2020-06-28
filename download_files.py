import json
import requests
import shutil
import os
import threading
import hashlib

#from datetime import datetime

header = {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.47 Safari/537.36'}

def make_folder(path):
    try:
        os.makedirs(path)
    except FileExistsError:
        # directory already exists
        pass

'''
# File Structure
> assets/
    > story.json
    > ureport.json
    > story_image/
        > story_image_[MD5 of URI].jpg
'''


apiURI = "https://ureport-offline.unicefbangladesh.org/api/"
local_dir = "app/src/main/assets/data"
make_folder(local_dir)

def md5_data(input):
    result = hashlib.md5(input.encode())
    return result.hexdigest()

def download_uri(file_uri):
    # Download Identifier
    r = requests.get(file_uri, headers=header)
    return r.content

f = open("app/src/main/assets/data/story.json")
story_json = json.load(f)
f.close()

# Parse Story
for data in story_json.get("data"):
    content_image = data.get("content_image")
    if content_image != "":
        image_file = "story_image_" + md5_data(content_image)
        image_path = local_dir + "/story_image/" + image_file

        # Download and Save Image if Not Exists
        if os.path.exists(image_path) == False:
            image_data = download_uri(content_image)
            f = open(image_path, "wb")
            f.write(image_data)
            f.close()

            print("Downloading: " + content_image + " > " + md5_data(content_image))
        else:
            print("Image Exists: " + content_image + " > " + md5_data(content_image))

    # Download Video
    story_video = data.get("story_video")
    if story_video != "":
        video_file = "story_video_" + md5_data(story_video)
        video_path = local_dir + "/story_video/" + video_file

        # Download and Save Image if Not Exists
        if os.path.exists(video_path) == False:
            video_data = download_uri(story_video)
            f = open(video_path, "wb")
            f.write(video_data)
            f.close()

            print("Downloading: " + story_video + " > " + md5_data(story_video))
        else:
            print("Image Exists: " + story_video + " > " + md5_data(story_video))