import torch
from ctc_forced_aligner import (
    load_audio,
    load_alignment_model,
    generate_emissions,
    preprocess_text,
    get_alignments,
    get_spans,
    postprocess_results,
)
import os
import json

os.environ["UROMAN_DIR"] = "uroman/uroman"

audio_path = "dataset/IK1/Its_my_house/Its_my_house6.wav"
text_path = "dataset/IK1/Its_my_house/Its_my_house.txt"
language = "rus"  # ISO-639-3 Language code
device = "cuda" if torch.cuda.is_available() else "cpu"
batch_size = 16

alignment_model, alignment_tokenizer = load_alignment_model(
    device,
    dtype=torch.float16 if device == "cuda" else torch.float32
)

audio_waveform = load_audio(audio_path, alignment_model.dtype, alignment_model.device)

with open(text_path, "r", encoding="utf-8") as f:
    lines = f.readlines()
text = "".join(line for line in lines).replace("\n", " ").strip()

emissions, stride = generate_emissions(
    alignment_model, audio_waveform, batch_size=batch_size
)

tokens_starred, text_starred = preprocess_text(
    text,
    romanize=True,
    language=language,
)

segments, scores, blank_token = get_alignments(
    emissions,
    tokens_starred,
    alignment_tokenizer,
)

spans = get_spans(tokens_starred, segments, blank_token)

word_timestamps = postprocess_results(text_starred, spans, stride, scores)

# Вывести результаты в консоль
print(word_timestamps)

output_path = "dataset/IK1/Its_my_house/alignment_results.json"
with open(output_path, "w", encoding="utf-8") as f:
    json.dump(word_timestamps, f, ensure_ascii=False, indent=4)
print(f"Результаты сохранены в {output_path}")
