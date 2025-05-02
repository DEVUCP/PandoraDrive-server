from typing import List

import requests

cur_folder_id = 1
cwd_files = []
cwd_valid = False

URL = "http://localhost:55555"


def validate_cwd():
    if cwd_valid:
        return
    cwd_files.clear()
    req = requests.get(f"{URL}/file?folder_id={cur_folder_id}")
    body: List[int] = req.json()

    for file_id in body:
        req = requests.get(f"{URL}/file?file_id={file_id}")
        if req.status_code == 200:
            cwd_files.append(req.json())


def ls():
    validate_cwd()
    for cwd_file in cwd_files:
        print(cwd_file["file_name"])


def rm(cmd: str):
    parts = cmd.split(" ")
    if len(parts) != 2:
        print("Valid format is rm <file_name>")
        return
    # TODO: Continue this


def restart():
    global cwd_valid
    cwd_valid = False


if __name__ == "__main__":
    running = True
    while running:
        cmd = input("> ").strip().lower()

        if cmd == "quit" or cmd == "exit":
            running = False
        elif cmd == "ls":
            ls()
        elif cmd == "restart":
            restart()
        elif cmd.startswith("rm"):
            rm(cmd)
        else:
            print(f"Invaild cmd: {cmd}")
