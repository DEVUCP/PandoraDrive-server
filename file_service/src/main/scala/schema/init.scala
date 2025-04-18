package schema

def initialize_schemas(): Unit =
  create_folder_metadata_table()
  create_file_metadata_table()
