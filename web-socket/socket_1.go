package main

import (
	"encoding/json"
	"bytes"
	"fmt"
	"net/http"
	"github.com/gorilla/websocket"
	// "time"
	"os"
)

type StockTrade struct {
	Data []struct {
		P float64 `json:"p"`
		S string  `json:"s"`
		T int64   `json:"t"`
		V float64 `json:"v"`
	} `json:"data"`
	Type string `json:"type"`
}

func main() {

	w, _, err := websocket.DefaultDialer.Dial("wss://ws.finnhub.io?token=", nil)
	if err != nil {
		panic(err)
	}
	defer w.Close()

	symbols := []string{os.Args[1]}
	for _, s := range symbols {
		msg, _ := json.Marshal(map[string]interface{}{"type": "subscribe", "symbol": s})
		w.WriteMessage(websocket.TextMessage, msg)
	}

	var msg StockTrade
	// start_time := time.Now().Unix()
	for {
		err := w.ReadJSON(&msg);
		if err != nil {
			panic(err)
		}
		fmt.Println("--------------------------")
		fmt.Println(msg)
		fmt.Println(msg.Data)

		if len(msg.Data) > 0 {
			fmt.Println(msg.Data[len(msg.Data)-1].S)
			fmt.Println(msg.Data[len(msg.Data)-1].P)

			body := fmt.Sprintf(`{"chat_id": -1001651805235, "text": "Latest price for %s: %f"}`, msg.Data[len(msg.Data)-1].S, msg.Data[len(msg.Data)-1].P)

			// if time.Now().Unix() - start_time > 1 {
			jsonBody := []byte(body)
			bodyReader := bytes.NewReader(jsonBody)
			requestURL := "https://api.telegram.org/bot<redacted>/sendMessage"
			// start_time = time.Now().Unix()
			fmt.Println("========================")
			fmt.Println("SENDING TO TELEGRAM")
			fmt.Println("========================")
			http.Post(requestURL, "application/json", bodyReader);
			// }
		}

		
		
	}

}