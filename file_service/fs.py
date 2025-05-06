import os
from typing import Dict

import requests

cur_folder: Dict = {}
cwd_files = []
cwd_valid = False

USER_ID = 1
URL = "http://localhost:55555"


def validate_folder():
    global cur_folder
    if cur_folder:
        return
    req = requests.get(f"{URL}/folder?user_id={USER_ID}")
    cur_folder = req.json()
    print("Current folder has been initiated")


def validate_cwd():
    validate_folder()
    global cwd_valid
    if cwd_valid:
        return
    cwd_files.clear()
    req = requests.get(f"{URL}/file?folder_id={cur_folder['folder_id']}")
    for file in req.json():
        cwd_files.append(file)
    cwd_valid = True


def ls():
    validate_cwd()
    for cwd_file in cwd_files:
        print(cwd_file["file_name"])


def rm(cmd: str):
    parts = cmd.split(" ")
    if len(parts) != 2:
        print("Valid format is rm <file_name>")
        return
    name = parts[1]
    validate_cwd()
    print(cwd_files)
    file_id = next(
        (file["file_id"] for file in cwd_files if file["file_name"] == name), None
    )
    if not file_id:
        print(f"File name {name} doesn't exist in CWD")
        return

    req = requests.delete(f"{URL}/file/delete?file_id={file_id}")
    print(f"File Deletion Request Status Code: {req.status_code}")
    restart()


def restart():
    global cwd_valid
    cwd_valid = False


def download(cmd: str):
    parts = cmd.split(" ")
    if len(parts) != 3:
        print("Invalid Format: download <file_name_in_current_folder> destination")
        return

    file_name = parts[1]
    dest = parts[2]
    if not os.path.isdir(dest):
        print("Invalid destination folder")
        return

    validate_cwd()
    file_id = next(
        (file["file_id"] for file in cwd_files if file["file_name"] == file_name), None
    )

    if not file_id:
        print(f"File {file_name} not found in cwd")
        return

    # Get File Data
    req = requests.get(f"{URL}/file/download?file_id={file_id}")
    if not req.status_code == 200:
        print(f"Something is wrong: Status Code= {req.status_code}")
        print(req.json())
        return
    data = req.json()
    download_link = data["download_link"]
    print(download_link)
    chunk_data = data["data"]

    print(f"Reconstructing the file {file_name}...")
    with open(os.path.join(dest, file_name), "wb") as f:
        for chunk in chunk_data:
            chunk_id, chunk_sequence, _ = chunk.values()
            print(chunk_id)
            response = requests.get(download_link, json={"chunk_id": chunk_id})
            if response.status_code == 200:
                f.write(response.content)
                print(f"Chunk #{chunk_sequence} downloaded successfully")
            else:
                print(
                    f"Chunk #{chunk_sequence} failed. Status Code: {response.status_code}"
                )
                print(f"Chunk #{chunk_sequence} didn't download: {response.text}")


def pwd():
    validate_folder()
    print(cur_folder["folder_name"])


if __name__ == "__main__":
    running = True
    while running:
        cmd = input("> ").strip().lower()

        try:
            if cmd in ["quit", "exit", "q"]:
                running = False
            elif cmd == "ls":
                ls()
            elif cmd == "restart":
                restart()
            elif cmd.startswith("rm"):
                rm(cmd)
            elif cmd.startswith("dl"):
                download(cmd)
            elif cmd.startswith("pwd"):
                pwd()
            else:
                print(f"Invaild cmd: {cmd}")
        except:
            print("Something wrong happened")
