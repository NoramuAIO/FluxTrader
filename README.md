# FluxTrader - Traveling Merchant System

A sophisticated Minecraft Spigot/Paper plugin that implements a traveling merchant system with random product sales, time-based availability, and a customizable GUI.

## Features

### 🔁 Automatic Refresh System
- Automatic refresh every 12 hours (configurable)
- Optional fixed-time refresh (e.g., 06:00, 12:00, 18:00, 00:00)
- Survives server restarts
- Persistent refresh time tracking

### 🎲 Random Product System
- Products randomly selected from pool
- Individual drop chance per item (%)
- Min/Max amount configuration
- Daily stock limits
- Same item won't appear twice in one day

### 🎨 Advanced GUI System
- 3 product display slots
- Real-time refresh countdown indicator
- Player profile button showing currency amounts
- Close button
- Background decoration items

### 💰 Multi-Currency System
- Vault economy support
- Multiple currency types: GOLD, DIAMOND, EMERALD, VAULT
- Per-item currency selection
- Each item has its own currency type

### ⏰ Time-Based Items
- Items available only at specific hours
- Seasonal items (by month)
- Weekly items (by day)
- Automatic filtering

### 📢 Announcement System
- Refresh notification when items update
- Countdown reminders (configurable minutes: 30, 15, 10, 5, 1, last 10 seconds)
- Server-wide broadcast support
- Customizable messages

### 📊 PlaceholderAPI Support
- `%fluxtrader_next_refresh%` - Time until next refresh
- `%fluxtrader_next_refresh_seconds%` - Seconds until refresh
- `%fluxtrader_items_count%` - Current item count

### 🛡️ Security Features
- Drag prevention - items cannot be dragged from GUI
- Shift-click prevention
- Only left-click purchases allowed
- Inventory synchronization

## Installation

### Requirements
- Spigot/Paper 1.20.4+
- Vault (for economy system)
- PlaceholderAPI (optional)
- Java 17+

### Steps
1. Build the project: `mvn clean package`
2. Copy the JAR file to `plugins` folder
3. Start the server
4. Configuration files will be auto-generated
5. Edit config files as needed and run `/fluxtrader reload`

## Commands

| Command | Description | Permission |
|---------|-------------|-----------|
| `/fluxtrader gui` | Open merchant GUI | - |
| `/fluxtrader info` | Show plugin information | - |
| `/fluxtrader reload` | Reload config and GUI | fluxtrader.admin |
| `/fluxtrader next` | Force immediate refresh | fluxtrader.admin |

## Configuration Files

### config.yml
- Refresh settings (interval or fixed times)
- Announcement settings (enabled, broadcast, reminder times)
- Time-based item configuration

### messages.yml
- All player messages (purchase success, errors, announcements)
- Command messages
- Customizable with placeholders (%amount%, %price%, %currency%, etc.)

### inventory.yml
- GUI layout and design
- Button names and descriptions
- Item display messages
- Profile description format

### items.yml
- Product definitions
- Material, price, currency type
- Drop chance, min/max amounts
- Daily limits
- Time-based availability (hours, days, months)

## Features Implemented

✅ Turkish player messages with normal characters  
✅ English console messages  
✅ Real-time GUI updates  
✅ Minute-based announcement system  
✅ Time-based product filtering  
✅ Tab completion for commands  
✅ PlaceholderAPI integration  
✅ Vault economy support  
✅ Drag prevention in GUI  
✅ Dynamic daily limit display  
✅ Multi-currency support  
✅ Configurable messages  

Available placeholders:
- `%amount%` - Item quantity
- `%item%` - Item name
- `%price%` - Item price
- `%currency%` - Currency type
- `%time%` - Time remaining
- `%items%` - Item count
- `%vault%`, `%gold%`, `%diamond%`, `%emerald%` - Currency amounts

## Time-Based Items Example

```yaml
item_night:
  id: "item_night"
  material: "DARK_OAK_LEAVES"
  display-name: "&9🌙 Night Treasure"
  available-hours: [20, 21, 22, 23, 0, 1, 2, 3, 4, 5]  # 20:00 - 05:59
  currency-type: "VAULT"
  price: 50
  daily-limit: 3

item_weekend:
  id: "item_weekend"
  material: "CAKE"
  display-name: "&d🎂 Weekend Cake"
  available-days: [6, 7]  # Saturday and Sunday
  currency-type: "VAULT"
  price: 50
  daily-limit: 10
```

## License

MIT License

## Support

For issues and suggestions, please open an issue on the project repository.
