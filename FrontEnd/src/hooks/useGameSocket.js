import { useEffect, useState, useCallback } from 'react';
import toast from 'react-hot-toast';
import socketService from '../services/websocketService';

export const useGameSocket = (roomId, playerId) => {
    const [gameState, setGameState] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!roomId) return;

        // 1. Connect
        socketService.connect();

        // 2. Wait for connection and subscribe
        const checkConnection = setInterval(() => {
            if (socketService.connected) {
                console.log(`Subscribing to /topic/game/${roomId}`);
                socketService.subscribe(`/topic/game/${roomId}`, (update) => {
                    if (update.type === 'UPDATE' || update.type === 'START' || update.type === 'WINNER') {
                        setGameState(update.payload);
                    } else if (update.type === 'ERROR') {
                        setError(update.content);
                    } else if (update.type === 'CHAT') {
                        const chat = update.payload;
                        setGameState(prev => ({
                            ...prev,
                            lastInteraction: {
                                type: update.type,
                                playerId: chat.playerId,
                                content: chat.message,
                                timestamp: chat.timestamp || Date.now()
                            }
                        }));
                    } else if (update.type === 'EMOJI') {
                        setGameState(prev => ({
                            ...prev,
                            lastInteraction: {
                                type: update.type,
                                playerId: update.payload,
                                content: update.content,
                                timestamp: Date.now()
                            }
                        }));
                    } else if (update.type === 'KICKED' && update.payload === playerId) {
                        toast.error(update.content || "Bạn đã bị mời ra khỏi phòng.");
                        window.location.href = "/";
                    }
                });

                // Join the game automatically
                socketService.send(`/app/game.join/${roomId}`, playerId);

                clearInterval(checkConnection);
            }
        }, 500);

        return () => {
            socketService.disconnect();
            clearInterval(checkConnection);
        };
    }, [roomId, playerId]);

    const startGame = useCallback(() => {
        socketService.send(`/app/game.start/${roomId}`, playerId);
    }, [roomId, playerId]);

    const toggleReady = useCallback(() => {
        socketService.send(`/app/game.ready/${roomId}`, playerId);
    }, [roomId, playerId]);

    const kickPlayer = useCallback((targetId) => {
        socketService.send(`/app/game.kick/${roomId}`, {
            hostId: playerId,
            targetId: targetId
        });
    }, [roomId, playerId]);

    const resetRoom = useCallback(() => {
        socketService.send(`/app/game.reset/${roomId}`, playerId);
    }, [roomId, playerId]);

    const playMove = useCallback((cards) => {
        socketService.send(`/app/game.play/${roomId}`, {
            playerId: playerId,
            cards: cards
        });
    }, [roomId, playerId]);

    const passTurn = useCallback(() => {
        socketService.send(`/app/game.pass/${roomId}`, playerId);
    }, [roomId, playerId]);

    const leaveRoom = useCallback(() => {
        socketService.send(`/app/game.leave/${roomId}`, playerId);
    }, [roomId, playerId]);

    const sendChat = useCallback((message) => {
        socketService.send(`/app/game.chat/${roomId}`, { playerId, message });
    }, [roomId, playerId]);

    const sendEmoji = useCallback((emoji) => {
        socketService.send(`/app/game.emoji/${roomId}`, { playerId, emoji });
    }, [roomId, playerId]);

    const sendAction = useCallback((action) => {
        socketService.send(`/app/game.action/${roomId}`, {
            playerId,
            ...action
        });
    }, [roomId, playerId]);

    return { gameState, error, startGame, toggleReady, resetRoom, kickPlayer, playMove, passTurn, sendAction, leaveRoom, sendChat, sendEmoji, setError };
};
