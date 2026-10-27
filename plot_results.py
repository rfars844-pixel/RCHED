import csv
import matplotlib.pyplot as plt
import numpy as np

with open("benchmark_results.csv", newline="", encoding="utf-8") as f:
    rows = list(csv.DictReader(f))

if not rows:
    raise SystemExit("benchmark_results.csv is empty. Run the Java benchmark first.")

algorithms = [row["Algorithm"] for row in rows]
case_names = [name for name in rows[0].keys() if name != "Algorithm"]
x = np.arange(len(algorithms))
width = 0.19

for i, case in enumerate(case_names):
    values = [int(row[case]) for row in rows]
    plt.bar(x + (i - (len(case_names) - 1) / 2) * width, values, width, label=case)

plt.yscale("log")
plt.xlabel("Sorting algorithm")
plt.ylabel("Execution time (microseconds, log scale)")
plt.title("Sorting Algorithm Performance by Input Pattern")
plt.xticks(x, algorithms, rotation=20, ha="right")
plt.legend()
plt.tight_layout()
plt.savefig("benchmark_chart.png", dpi=200)
print("Created benchmark_chart.png")
