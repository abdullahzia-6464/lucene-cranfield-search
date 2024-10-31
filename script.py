# Input and output file paths
input_file_path = './src/resources/cranqrel'
output_file_path = './src/resources/cranqrel_converted'

def map_relevance_score(relevance):
    if relevance == '-1':
        return '5'
    else:
        return relevance  # Keep other values unchanged

with open(input_file_path, 'r') as file:
    cranqrel_content = file.readlines()

# Processing each line and applying the relevance score mapping
formatted_content = []
for line in cranqrel_content:
    parts = line.strip().split()
    if len(parts) == 3:  # Ensure we only process lines with the expected format
        query_id, doc_id, relevance = parts
        mapped_relevance = map_relevance_score(relevance)
        formatted_content.append(f"{query_id} 0 {doc_id} {mapped_relevance}\n")


with open(output_file_path, 'w') as formatted_file:
    formatted_file.writelines(formatted_content)

print(f"Formatted qrels file saved to {output_file_path}")