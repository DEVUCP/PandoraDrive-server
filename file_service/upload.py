import argparse
import datetime
import json
import mimetypes
import os
import sys
from typing import Dict

import requests

URL = "http://localhost:55555"
FOLDER_ID = -1
USER_ID = 1


def calc_folder_id():
    global FOLDER_ID
    req = requests.get(f"{URL}/folder?user_id={USER_ID}")
    FOLDER_ID = req.json()["folder_id"]
    print(f"FolderId is {FOLDER_ID}")


def upload_file(file_path: str):
    """Main upload function"""
    if not os.path.exists(file_path):
        print(f"Error: File not found - {file_path}", file=sys.stderr)
        sys.exit(1)

    stat = gather_stat(file_path)

    # TODO: Get these when the project finishes
    stat["folder_id"] = FOLDER_ID
    stat["user_id"] = USER_ID

    print("Status: ")
    print(json.dumps(stat, indent=4))

    try:
        init_data = init_upload(stat)
    except requests.exceptions.RequestException as e:
        print(f"Error: Failed to initialize upload - {str(e)}", file=sys.stderr)
        sys.exit(1)

    message = init_data["message"]
    token = init_data["token"]
    upload_link = init_data["upload_link"]
    complete_link = init_data["complete_link"]
    chunk_size = init_data["chunk_size"]

    print(f"Initilization message: {message}")

    try:
        with open(file_path, "rb") as f:
            file_size = os.path.getsize(file_path)
            total_chunks = (
                file_size + chunk_size - 1
            ) // chunk_size  # Ceiling division

            for chunk_index in range(total_chunks):
                # Calculate actual chunk size (full size or remainder for last chunk)
                current_chunk_size = min(
                    chunk_size, file_size - (chunk_index * chunk_size)
                )
                chunk = f.read(current_chunk_size)

                upload_chunk(
                    upload_link=upload_link,
                    chunk=chunk,
                    chunk_sequence=chunk_index,
                    token=token,
                    chunk_size=current_chunk_size,  # Send actual chunk size
                )
                print(f"Chunk #{chunk_index} uploaded")
    except:
        sys.exit(1)

    complete_request(complete_link, token)


def complete_request(
    complete_link: str,
    token: str,
):
    body = {"token": token}
    print(body)
    req = requests.post(complete_link, json=body)
    print(f"Complete Response Done. Status: {req.status_code}")
    if req.content:
        print(req.content)


def gather_stat(file_path: str):
    """Collect file metadata statistics"""
    mime_type, _ = mimetypes.guess_type(file_path)
    if mime_type is None:
        mime_type = "application/octet-stream"

    stat = os.stat(file_path)

    created_at = datetime.datetime.fromtimestamp(stat.st_ctime).strftime("%Y-%m-%d")
    modified_at = datetime.datetime.fromtimestamp(stat.st_mtime).strftime("%Y-%m-%d")
    return {
        "file_name": os.path.basename(file_path),
        "size_bytes": stat.st_size,
        "mime_type": mime_type,
        "created_at": created_at,
        "modified_at": modified_at,
    }


def init_upload(body: dict) -> Dict:
    """Initialize file upload and return token"""
    req = requests.post(f"{URL}/file/upload", json=body)
    json_response = req.json()
    print(json_response)
    print(req.status_code)
    assert req.ok, "The initialization failed"
    return json_response


def upload_chunk(
    upload_link: str, chunk: bytes, chunk_sequence: int, chunk_size: int, token: int
):
    """Upload one Chunk with the specific sequence, chunk_size"""
    metadata = {
        "chunk_sequence": chunk_sequence,
        "chunk_size": chunk_size,
        "token": token,
    }

    requests.post(
        upload_link,
        files={
            "metadata": (None, json.dumps(metadata), "application/json"),
            "chunk": (f"chunk_{chunk_sequence}.bin", chunk, "application/octet-stream"),
        },
    )


def main():
    """Parse arguments and execute upload"""
    parser = argparse.ArgumentParser(description="File upload utility")
    parser.add_argument("file_path", help="Path to the file to upload")

    args = parser.parse_args()
    upload_file(args.file_path)


if __name__ == "__main__":
    # Initialize MIME type database
    mimetypes.init()
    calc_folder_id()
    main()
