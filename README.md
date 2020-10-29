# Teamspeak 3 Telegram Notifier

Sends a message to a telegram group, pins that message (if permissions) 
and updates that message with currently connected users.

## Configuration

Set the following environment variables:

| ENV | Description |
|-----|-------------|
| TELEGRAM_BOT_NAME | Telegram Bot Name |
| TELEGRAM_BOT_TOKEN | Telegram Bot Token |
| CONFIG_DIR | Path to config location to preserve state between restarts |