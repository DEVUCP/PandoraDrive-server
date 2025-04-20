import argparse
import datetime
import json
import mimetypes
import os
import sys
from typing import Tuple

import httplib2

httplib2.debuglevel = 1

import requests

URL = "http://localhost:55551"


def upload_file(file_path: str):
    """Main upload function"""
    if not os.path.exists(file_path):
        print(f"Error: File not found - {file_path}", file=sys.stderr)
        sys.exit(1)

    stat = gather_stat(file_path)

    # TODO: Get these when the project finishes
    stat["folder_id"] = 1
    stat["owner_id"] = 1

    try:
        token, chunk_size = init_upload(stat)
    except requests.exceptions.RequestException as e:
        print(f"Error: Failed to initialize upload - {str(e)}", file=sys.stderr)
        sys.exit(1)

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
                    chunk=chunk,
                    chunk_sequence=chunk_index + 1,
                    token=token,
                    chunk_size=current_chunk_size,  # Send actual chunk size
                )
    except:
        sys.exit(1)


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


def init_upload(body: dict) -> Tuple[str, int]:
    """Initialize file upload and return token"""
    req = requests.post(f"{URL}/file/upload/init", json=body)
    assert req.status_code == 200, "The initialization failed"
    json_response = req.json()
    return (
        json_response["token"],
        1024 * 1024,
    )  # TODO: Adjust accordingly to make the init calculate the regular chunk size used by the server


def upload_chunk(chunk: bytes, chunk_sequence: int, chunk_size, token: str):
    metadata = {
        "chunk_sequence": chunk_sequence,
        "chunk_size": chunk_size,
        "token": token,
    }
    print(json.dumps(metadata))
    # Using multipart
    req = requests.post(
        f"{URL}/file/upload/chunk",
        json=metadata,
        files={
            "metadata": (None, json.dumps(metadata), "application/json"),
            "file": (f"chunk_{chunk_sequence}.bin", chunk, "application/octet-stream"),
        },
    )
    print(req.json())
    assert req.status_code == 200, f"Failed to upload chunk {chunk_sequence + 1}"


def main():
    """Parse arguments and execute upload"""
    parser = argparse.ArgumentParser(description="File upload utility")
    parser.add_argument("file_path", help="Path to the file to upload")

    args = parser.parse_args()
    upload_file(args.file_path)


if __name__ == "__main__":
    # Initialize MIME type database
    mimetypes.init()
    main()
