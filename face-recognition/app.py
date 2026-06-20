from flask import Flask, request, jsonify
import face_recognition
import numpy as np
import os
import uuid
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# ================= PATH =================
FACE_DB = "known_faces"   # face encodings (.npy)
RAW_FACE_DB = "raw_faces" # preview/captured images (.jpg)

os.makedirs(FACE_DB, exist_ok=True)
os.makedirs(RAW_FACE_DB, exist_ok=True)

# ================= SAFE EMAIL =================
def safe_email(email):
    return email.replace("@", "_at_").replace(".", "_")


# ================= REGISTER FACE =================
@app.route("/register-face", methods=["POST"])
def register_face():

    image = request.files.get("image")
    email = request.form.get("email")

    if not image or not email:
        return jsonify({"status": "INVALID_REQUEST"}), 400

    filename = safe_email(email)

    temp_file = f"temp_{uuid.uuid4()}.jpg"
    image.save(temp_file)

    RAW_FACE_DB = "raw_faces"
    os.makedirs(RAW_FACE_DB, exist_ok=True)

    try:
        img = face_recognition.load_image_file(temp_file)
        encodings = face_recognition.face_encodings(img)

        if len(encodings) != 1:
            return jsonify({"status": "INVALID_FACE"}), 400

        # 1. Save biometric encoding
        np.save(
            os.path.join(FACE_DB, filename + ".npy"),
            encodings[0]
        )

        # 2. Save raw preview image
        image.save(
            os.path.join(RAW_FACE_DB, filename + ".jpg")
        )

        return jsonify({
            "status": "FACE_REGISTERED"
        })

    finally:
        if os.path.exists(temp_file):
            os.remove(temp_file)

# ================= FACE LOGIN =================
@app.route("/face-login", methods=["POST"])
def face_login():

    image = request.files.get("image")
    email = request.form.get("email")

    if not image or not email:
        return jsonify({"status": "INVALID_REQUEST"}), 400

    temp_file = f"temp_{uuid.uuid4()}.jpg"
    image.save(temp_file)

    try:
        img = face_recognition.load_image_file(temp_file)
        encodings = face_recognition.face_encodings(img)

        if len(encodings) != 1:
            return jsonify({"status": "NO_FACE"}), 400

        unknown_encoding = encodings[0]

        filename = safe_email(email)
        path = os.path.join(FACE_DB, filename + ".npy")

        if not os.path.exists(path):
            return jsonify({"status": "USER_NOT_FOUND"})

        saved_encoding = np.load(path, allow_pickle=True)

        match = face_recognition.compare_faces(
            [saved_encoding],
            unknown_encoding,
            tolerance=0.45
        )

        if match[0]:
            return jsonify({
                "status": "MATCH",
                "email": email
            })

        return jsonify({"status": "NO_MATCH"})

    finally:
        if os.path.exists(temp_file):
            os.remove(temp_file)

# ================= EXAM PROCTORING =================
@app.route("/check-face", methods=["POST"])
def check_face():

    image = request.files.get("image")
    email = request.form.get("email")

    if not image or not email:
        return jsonify({"status": "INVALID_REQUEST"}), 400

    temp_file = f"temp_{uuid.uuid4()}.jpg"
    image.save(temp_file)

    try:
        img = face_recognition.load_image_file(temp_file)
        encodings = face_recognition.face_encodings(img)

        if len(encodings) == 0:
            return jsonify({"status": "NO_FACE"})

        if len(encodings) > 1:
            return jsonify({"status": "MULTIPLE_FACES"})

        unknown_encoding = encodings[0]

        filename = safe_email(email)
        path = os.path.join(FACE_DB, filename + ".npy")

        if not os.path.exists(path):
            return jsonify({"status": "USER_NOT_FOUND"})

        saved_encoding = np.load(
            path,
            allow_pickle=True
        )

        match = face_recognition.compare_faces(
            [saved_encoding],
            unknown_encoding,
            tolerance=0.45
        )

        if match[0]:
            return jsonify({"status": "OK"})
        else:
            return jsonify({"status": "NOT_MATCH"})

    finally:
        if os.path.exists(temp_file):
            os.remove(temp_file)


# ================= SERVER =================
if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000,
        debug=True
    )