from typing import List

import requests

cur_folder_id = 1
cwd_files = []
cwd_valid = False

URL = "http://localhost:55555"


def validate_cwd():
    global cwd_valid
    if cwd_valid:
        return
    cwd_files.clear()
    req = requests.get(f"{URL}/file?folder_id={cur_folder_id}")
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
            else:
                print(f"Invaild cmd: {cmd}")
        except Exception:
            print("Something wrong happened")
