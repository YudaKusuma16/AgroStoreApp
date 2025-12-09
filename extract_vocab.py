import joblib
import json
import numpy as np

# Load model dan TF-IDF vectorizer yang sudah di-training
model = joblib.load('healthbot_model.pkl')
tfidf_vectorizer = joblib.load('tfidf_vectorizer.pkl')

# Get vocabulary dari TF-IDF
vocabulary = tfidf_vectorizer.vocabulary_

# Get feature names
feature_names = tfidf_vectorizer.get_feature_names_out()

# Buat vocabulary mapping yang lengkap
vocab_mapping = {
    'vocabulary': vocabulary,
    'feature_names': feature_names.tolist(),
    'vocab_size': len(vocabulary),
    'idf_scores': tfidf_vectorizer.idf_.tolist()
}

# Save ke JSON
with open('tfidf_vocabulary.json', 'w', encoding='utf-8') as f:
    json.dump(vocab_mapping, f, ensure_ascii=False, indent=2)

# Print some info
print(f"Vocabulary size: {len(vocabulary)}")
print(f"First 100 features: {feature_names[:100].tolist()}")

# Buat vocabulary yang ringan untuk Android (hanya top features)
top_features = {}
for idx, feature in enumerate(feature_names):
    top_features[feature] = idx
    if idx >= 2000:  # Limit untuk Android
        break

# Save simplified vocab untuk Android
simplified_vocab = {
    'word_to_index': top_features,
    'total_features': len(feature_names)
}

with open('android_vocabulary.json', 'w', encoding='utf-8') as f:
    json.dump(simplified_vocab, f, ensure_ascii=False, indent=2)

print(f"Simplified vocabulary saved with {len(top_features)} words")