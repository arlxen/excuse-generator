import json
import sys
from data_social import category_meta as meta_social, excuses as excuses_social
from data_work import category_meta as meta_work, excuses as excuses_work
from data_school import category_meta as meta_school, excuses as excuses_school
from data_fitness import category_meta as meta_fitness, excuses as excuses_fitness

categories = [
    (meta_social, excuses_social),
    (meta_work, excuses_work),
    (meta_school, excuses_school),
    (meta_fitness, excuses_fitness),
]

REQUIRED_LANGUAGES = ["pl", "en", "ar", "es", "fr"]

output_data = {
    "categories": {}
}

total_excuses = 0

for meta, excuses in categories:
    cat_id = meta["id"]
    print(f"Validating category '{cat_id}'...")

    # Validate category metadata
    for lang in REQUIRED_LANGUAGES:
        if lang not in meta["name"] or not meta["name"][lang].strip():
            print(f"Error: Missing or empty category name for language '{lang}' in category '{cat_id}'")
            sys.exit(1)

    if len(excuses) != 20:
        print(f"Error: Category '{cat_id}' has {len(excuses)} excuses, expected 20.")
        sys.exit(1)

    validated_excuses = []
    for idx, excuse in enumerate(excuses, 1):
        for lang in REQUIRED_LANGUAGES:
            if lang not in excuse or not excuse[lang].strip():
                print(f"Error: Excuse #{idx} in category '{cat_id}' is missing language '{lang}' or it is empty.")
                sys.exit(1)
        validated_excuses.append(excuse)

    total_excuses += len(validated_excuses)
    output_data["categories"][cat_id] = {
        "name": meta["name"],
        "excuses": validated_excuses
    }

output_path = "/home/user/gemini/excuses/excuses.json"
with open(output_path, "w", encoding="utf-8") as f:
    json.dump(output_data, f, ensure_ascii=False, indent=2)

print(f"Successfully generated {output_path} with {len(output_data['categories'])} categories and {total_excuses} total excuses.")
