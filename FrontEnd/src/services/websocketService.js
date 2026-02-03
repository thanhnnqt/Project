import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
    }

    connect() {
        if (this.connected || (this.client && this.client.connected)) {
            console.log('WebSocket already connected or connecting...');
            return;
        }

        const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
        const WS_URL = API_URL.replace(/\/api\/?$/, "") + "/ws";
        console.log('Initiating WebSocket connection to ' + WS_URL);
        const socket = new SockJS(WS_URL);
        this.client = Stomp.over(socket);

        // Ensure debug logs go to console
        this.client.debug = (str) => console.log('STOMP: ' + str);

        this.client.connect({}, (frame) => {
            console.log('Connected to WebSocket! Frame: ' + frame);
            this.connected = true;
        }, (error) => {
            console.error('WebSocket Connection Error: ', error);
            this.connected = false;
        });
    }

    subscribe(topic, callback) {
        if (this.client && this.connected) {
            console.log('Subscribing to: ' + topic);
            return this.client.subscribe(topic, (message) => {
                const body = JSON.parse(message.body);
                console.log('Received message from ' + topic, body);
                callback(body);
            });
        }
        console.warn('Cannot subscribe, not connected yet to: ' + topic);
        return null;
    }

    send(destination, payload) {
        if (this.client && this.connected) {
            console.log('Sending to ' + destination, payload);
            this.client.send(destination, {}, JSON.stringify(payload));
        } else {
            console.warn('Cannot send, not connected yet to: ' + destination);
        }
    }

    disconnect() {
        if (this.client && this.connected) {
            console.log('Disconnecting WebSocket...');
            this.client.disconnect(() => {
                console.log('WebSocket disconnected successfully');
            });
            this.connected = false;
        }
    }
}

const socketService = new WebSocketService();
export default socketService;
