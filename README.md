# Lucene Search Engine for Cranfield Dataset

This project implements a search engine using Apache Lucene for indexing and querying the Cranfield dataset. It supports different analyzers (Standard, English, Whitespace) and scoring models (VSM, BM25, Boolean, LMD). The project also provides evaluation using TREC Eval to measure the effectiveness of the search engine.

## Requirements

- Java (JDK 11 or later)
- Apache Maven
- Python (for result conversion)
- TREC Eval (for evaluation)

## Project Structure

The project consists of three main components:
- **ParseCranfield.java**: Parses the Cranfield dataset for indexing.
- **CreateIndex.java**: Creates an index using different analyzers.
- **QueryIndex.java**: Queries the index with different similarity models and analyzers.

## How to Run the Project

### Step 1: Index the Cranfield Dataset

To create the index, use the `CreateIndex` class with the desired analyzer. The available analyzers are `standard`, `whitespace`, and `english`.

```bash
mvn exec:java -Dexec.mainClass="com.assignment.lucene.CreateIndex" -Dexec.args="standard"
```
Replace standard with whitespace or english depending on the desired analyzer.
Step 2: Query the Index
- You can query the index using different similarity models (vsm, bm25, boolean, lmd) and analyzers (standard, whitespace, english). You can run the query in batch mode (for running all queries) or interactive mode (for manual input).

```bash
mvn exec:java -Dexec.mainClass="com.assignment.lucene.QueryIndex" -Dexec.args="batch vsm standard"
```

- Replace `batch` with `interactive` for interactive mode.
- Replace `vsm` with `bm25`, `boolean`, or `lmd` for the similarity model.
- Replace `standard` with `whitespace` or `english` for the analyzer.

### Step 3: Convert Results for TREC Eval

After running queries, you can convert the results files using the provided Python script (`script.py`):

This will process the results files for evaluation using TREC Eval.

### Step 4: Evaluate with TREC Eval

Once the results are converted, you can evaluate the performance using TREC Eval. From within the `trec_eval` folder, use the following command:
```bash
./trec_eval <qrels_file> <results_file>
```
Replace `<qrels_file>` with the path to the qrels file.
Replace `<results_file>` with the path to the results file.

For example:
```bash
./trec_eval -q ../cranqrel_converted ../lucene-search-engine/results/standard_vsm_results.txt
```

### Project Structure
```
.
├── src
│   ├── main
│   │   └── java
│   │       └── com
│   │           └── assignment
│   │               └── lucene
│   │                   ├── ParseCranfield.java
│   │                   ├── CreateIndex.java
│   │                   └── QueryIndex.java
│   └── resources              
│       ├── cran.all.1400       # Cranfield documents file
│       ├── cran.qry            # Query file
│       ├── cranqrel            # Qrels (relevance judgments) file
│       └── cranqrel.readme     # Readme explaining qrel format
├── results
│   └── (output result files)
├── script.py                   # Python script to convert results for TREC Eval
└── trec_eval
    └── (TREC Eval executable)

```