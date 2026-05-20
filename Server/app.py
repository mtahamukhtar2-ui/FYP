import uvicorn
from fastapi import FastAPI, File, UploadFile
from PIL import Image
import numpy as np
import tensorflow as tf
import io
import json
from fastapi.responses import JSONResponse
from tensorflow.keras.applications.efficientnet import preprocess_input as efficientnet_preprocess

app = FastAPI()
# 🍃 LEAF DETECTION — TFLite
leaf_interpreter = tf.lite.Interpreter(model_path="leaf_classifier.tflite")
leaf_interpreter.allocate_tensors()
leaf_input_details  = leaf_interpreter.get_input_details()
leaf_output_details = leaf_interpreter.get_output_details()
LEAF_CONFIDENCE_THRESHOLD = 0.80
LEAF_IS_HIGH_OUTPUT = False

def is_leaf(image: Image.Image) -> bool:
    img = image.resize((224, 224))
    img_array = np.array(img).astype(np.float32) / 255.0
    img_input = np.expand_dims(img_array, axis=0)

    leaf_interpreter.set_tensor(leaf_input_details[0]['index'], img_input)
    leaf_interpreter.invoke()
    prediction = leaf_interpreter.get_tensor(leaf_output_details[0]['index'])[0][0]

    leaf_score = float(prediction) if LEAF_IS_HIGH_OUTPUT else float(1.0 - prediction)
    print(f"[LeafCheck] score={leaf_score:.3f}, threshold={LEAF_CONFIDENCE_THRESHOLD}")
    return leaf_score >= LEAF_CONFIDENCE_THRESHOLD


# --- 1. Orange CONFIGURATION 
CITRUS_MODEL_PATH = "SweetOrange_model.tflite" 

CITRUS_CLASS_NAMES = [
    'Citrus canker', 'Citrus greening', 'Citrus mealybugs', 
    'Die back', 'Foliage damaged', 'Healthy leaf', 
    'Powdery mildew', 'Shot hole', 'Spiny whitefly', 
    'Yellow dragon', 'Yellow leaves'
]


# --- CORN CONFIGURATION ---
CORN_MODEL_PATH = "CoRn_model.tflite"
CORN_CLASS_NAMES = ['Common Rust', 'Corn Leaf Blight', 'Gray Leaf Spot', 'Healthy', 'Insects damage']

# --- APPLE CONFIGURATION ---
APPLE_MODEL_PATH = "apple_modelH.tflite"
APPLE_CLASS_NAMES = ['Apple Scab', 'Black Rot', 'Cedar Apple Rust', 'Healthy']

# --- POTATO CONFIGURATION ---
POTATO_MODEL_PATH = "pot_ato_model.tflite"
POTATO_CLASS_NAMES = ['Potato___Early_blight', 'Potato___Late_blight', 'Potato___healthy']


# --- LEMON CONFIGURATION ---
LEMON_MODEL_PATH = "lemon_disease_model.tflite"

# Replace these with the exact 9 lines from your lemon_labels.txt file
LEMON_CLASS_NAMES = [
    "Anthracnose",
    "Bacterial Blight",
    "Citrus Canker",
    "Curl Virus",
    "Deficiency Leaf",
    "Dry Leaf",
    "Healthy Leaf",
    "Sooty Mould",
    "Spider Mites"
]


# --- Mango CONFIGURATION ---
MANGO_MODEL_PATH = "Mango_model.tflite"
MANGO_CLASS_NAMES = ['Anthracnose', 'Bacterial Canker', 'Cutting Weevil', 'Die Back', 'Gall Midge', 'Healthy', 'Powdery Mildew', 'Sooty Mould', 'die back']


# --- 2. LOAD Orange TFLITE MODEL ---
citrus_interpreter = tf.lite.Interpreter(model_path=CITRUS_MODEL_PATH)
citrus_interpreter.allocate_tensors()
citrus_input_details = citrus_interpreter.get_input_details()
citrus_output_details = citrus_interpreter.get_output_details()

# --- LOAD CORN TFLITE MODEL ---
corn_interpreter = tf.lite.Interpreter(model_path=CORN_MODEL_PATH)
corn_interpreter.allocate_tensors()
corn_input_details = corn_interpreter.get_input_details()
corn_output_details = corn_interpreter.get_output_details()

# --- LOAD APPLE TFLITE MODEL ---
apple_interpreter = tf.lite.Interpreter(model_path=APPLE_MODEL_PATH)
apple_interpreter.allocate_tensors()
apple_input_details = apple_interpreter.get_input_details()
apple_output_details = apple_interpreter.get_output_details()

# --- LOAD POTATO TFLITE MODEL ---
potato_interpreter = tf.lite.Interpreter(model_path=POTATO_MODEL_PATH)
potato_interpreter.allocate_tensors()
potato_input_details = potato_interpreter.get_input_details()
potato_output_details = potato_interpreter.get_output_details()


# --- LOAD LEMON MODEL ---
lemon_interpreter = tf.lite.Interpreter(model_path=LEMON_MODEL_PATH)
lemon_interpreter.allocate_tensors()
lemon_input_details = lemon_interpreter.get_input_details()
lemon_output_details = lemon_interpreter.get_output_details()

# --- LOAD Mango TFLITE MODEL ---
mango_interpreter = tf.lite.Interpreter(model_path=MANGO_MODEL_PATH)
mango_interpreter.allocate_tensors()
mango_input_details = mango_interpreter.get_input_details()
mango_output_details = mango_interpreter.get_output_details()

@app.get("/")
def home():
    return {"message": "Orange Disease Detection API (TFLite Version) is Running!"}

# --- Orange's Code ---   
@app.post("/predict")
async def predict_citrus(file: UploadFile = File(...)):
    # --- PREPROCESS IMAGE ---
    image_data = await file.read()
    image = Image.open(io.BytesIO(image_data)).convert("RGB")
    if not is_leaf(image):
        return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })
    
    # 1. Custom Resize (Using the 244x244 specification)
    image = image.resize((224, 224)) 

    # Convert to numpy array and set to float32
    input_data = np.array(image).astype(np.float32)/255.0
    
    # Add the batch dimension (1, 224, 224, 3)
    input_data = np.expand_dims(input_data, axis=0)

    # --- RUN INFERENCE ---
    citrus_interpreter.set_tensor(citrus_input_details[0]['index'], input_data)
    citrus_interpreter.invoke()
    output_data = citrus_interpreter.get_tensor(citrus_output_details[0]['index'])

    # --- PROCESS RESULTS ---
    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0]))
    predicted_class = CITRUS_CLASS_NAMES[prediction]



    return {
        "class": predicted_class,
        "confidence": confidence
    }

        
# --- Corn's Code ---
@app.post("/predict_corn")
async def predict_corn(file: UploadFile = File(...)):
    # --- PREPROCESS IMAGE ---
    image_data = await file.read()
    image = Image.open(io.BytesIO(image_data)).convert("RGB")
    if not is_leaf(image):
        return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })
    image = image.resize((224, 224))
    
    input_data = np.array(image).astype(np.float32)/255.0
    input_data = np.expand_dims(input_data, axis=0)

    # --- RUN INFERENCE (Using Corn Interpreter) ---
    corn_interpreter.set_tensor(corn_input_details[0]['index'], input_data)
    corn_interpreter.invoke()
    output_data = corn_interpreter.get_tensor(corn_output_details[0]['index'])

    # --- PROCESS RESULTS (Using Corn Classes) ---
    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0]))
    predicted_class = CORN_CLASS_NAMES[prediction]

  
    return {
        "class": predicted_class,
        "confidence": confidence
    }

# --- Apple's Code ---
@app.post("/predict_apple")
async def predict_apple(file: UploadFile = File(...)):
    # --- PREPROCESS IMAGE ---
    image_data = await file.read()
    image = Image.open(io.BytesIO(image_data)).convert("RGB")
    if not is_leaf(image):
        return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })
    image = image.resize((224, 224))
    
    input_data = np.array(image).astype(np.float32)/255.0 
    input_data = np.expand_dims(input_data, axis=0)

    # --- RUN INFERENCE (Using Apple Interpreter) ---
    apple_interpreter.set_tensor(apple_input_details[0]['index'], input_data)
    apple_interpreter.invoke()
    output_data = apple_interpreter.get_tensor(apple_output_details[0]['index'])

    # --- PROCESS RESULTS (Using Apple Classes) ---
    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0]))
    predicted_class = APPLE_CLASS_NAMES[prediction]

  

    return {
        "class": predicted_class,
        "confidence": confidence
    }
    
# --- Potato's Code ---
@app.post("/predict_potato")
async def predict_potato(file: UploadFile = File(...)):
    # --- PREPROCESS IMAGE ---
    image_data = await file.read()
    image = Image.open(io.BytesIO(image_data)).convert("RGB")
    if not is_leaf(image):
        return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })
    image = image.resize((224, 224))
    
    input_data = np.array(image) / 255.0
    input_data = np.expand_dims(input_data, axis=0).astype(np.float32)

    # --- RUN INFERENCE (Using Potato Interpreter) ---
    potato_interpreter.set_tensor(potato_input_details[0]['index'], input_data)
    potato_interpreter.invoke()
    output_data = potato_interpreter.get_tensor(potato_output_details[0]['index'])

    # --- PROCESS RESULTS (Using Potato Classes) ---
    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0]))
    predicted_class = POTATO_CLASS_NAMES[prediction]

    

    return {
        "class": predicted_class,
        "confidence": confidence
    }   

# --- Lemon's Code ---
@app.post("/predict_lemon")
async def predict_lemon(file: UploadFile = File(...)): 
    try:
        # --- PREPROCESS IMAGE ---
        image_data = await file.read()
        image = Image.open(io.BytesIO(image_data)).convert("RGB")
        
        if not is_leaf(image):
            return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })

        image = image.resize((224, 224))
        
        # EfficientNetB0 Normalization
        input_data = np.array(image).astype(np.float32)
        input_data = np.expand_dims(input_data, axis=0)

        # --- RUN INFERENCE ---
        lemon_interpreter.set_tensor(lemon_input_details[0]['index'], input_data)
        lemon_interpreter.invoke()
        output_data = lemon_interpreter.get_tensor(lemon_output_details[0]['index'])

        # --- PROCESS RESULTS ---
        prediction = np.argmax(output_data[0])
        confidence = float(np.max(output_data[0]))
        predicted_class = LEMON_CLASS_NAMES[prediction]
        
        display_name = predicted_class

    
        return {
                "class": display_name,
                "confidence": confidence
            }
            
    except Exception as e:
        return {"error": str(e), "message": "The code crashed before finishing the prediction."}
    

# --- Mango's Code ---
@app.post("/predict_mango")
async def predict_mango(file: UploadFile = File(...)): # Fixed function name
    # --- PREPROCESS IMAGE ---
    image_data = await file.read()
    image = Image.open(io.BytesIO(image_data)).convert("RGB")
    if not is_leaf(image):
        return JSONResponse(status_code=400, content={
            "error": "Not a leaf",
            "message": "Please upload a clear image of a PLANT leaf."
        })
    image = image.resize((224, 224))
    
    input_data = np.array(image).astype(np.float32)/255.0
    input_data = np.expand_dims(input_data, axis=0)

    # --- RUN INFERENCE ---
    mango_interpreter.set_tensor(mango_input_details[0]['index'], input_data)
    mango_interpreter.invoke()
    output_data = mango_interpreter.get_tensor(mango_output_details[0]['index'])

    # --- PROCESS RESULTS ---
    prediction = np.argmax(output_data[0])
    confidence = float(np.max(output_data[0]))
    predicted_class = MANGO_CLASS_NAMES[prediction]
    
    return {
        "class": predicted_class, # Fixed the NameError crash
        "confidence": confidence
    }

    
