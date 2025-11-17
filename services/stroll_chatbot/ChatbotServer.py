from flask import Flask, request, jsonify

# CORS는 선택사항 (pip install flask-cors 로 설치 가능)
try:
    from flask_cors import CORS
    CORS_AVAILABLE = True
except ImportError:
    CORS_AVAILABLE = False

app = Flask(__name__)
if CORS_AVAILABLE:
    CORS(app)  # CORS 활성화 (필요한 경우)

@app.route('/')
def health_check():
    """서버 상태 확인"""
    return jsonify({
        "status": "ok",
        "message": "Chatbot server is running"
    }), 200

@app.route('/chat', methods=['POST'])
def chat():
    """채팅 메시지 처리"""
    try:
        data = request.get_json()
        
        # 요청 데이터 검증
        if not data or 'message' not in data:
            return jsonify({
                "error": "Invalid request. 'message' field is required."
            }), 400
        
        user_message = data['message']
        
        # TODO: 여기에 실제 챗봇 로직 구현
        # 예: OpenAI API 호출, Chroma 검색 등
        
        response = {
            "response": f"Echo: {user_message}",
            "status": "success"
        }
        
        return jsonify(response), 200
        
    except Exception as e:
        return jsonify({
            "error": str(e),
            "status": "error"
        }), 500

if __name__ == '__main__':
    # 개발 환경
    app.run(debug=True, host='0.0.0.0', port=5000)