# Thay đổi Logic Staff Engagement

## Tổng quan
Đã thêm logic để ngăn bot phản hồi và ngăn cuộc trò chuyện đổi participant khi staff agent được kích hoạt. Chỉ khi staff thay đổi agent thì bot mới có thể trả lời lại như cũ.

## Các thay đổi chính

### 1. Thêm trường `isStaffEngaged` vào Conversation
- **File**: `src/main/java/com/example/maschat/domain/Conversation.java`
- **Thay đổi**: Thêm cột `is_staff_engaged` (BOOLEAN, default FALSE)
- **Mục đích**: Theo dõi trạng thái staff engagement của cuộc trò chuyện

### 2. Cập nhật logic trong `sendUserMessage`
- **File**: `src/main/java/com/example/maschat/service/ChatService.java`
- **Thay đổi**: Kiểm tra `isStaffEngaged` trước khi gửi phản hồi bot
- **Logic**: Nếu `isStaffEngaged = true`, bỏ qua việc gửi phản hồi bot

### 3. Cập nhật logic trong `routeToStaffAgent`
- **File**: `src/main/java/com/example/maschat/service/ChatService.java`
- **Thay đổi**: Set `isStaffEngaged = true` khi chuyển sang staff agent
- **Logic**: Khi user yêu cầu nói chuyện với staff, set flag này thành true

### 4. Cập nhật logic trong `updateConversationAgents`
- **File**: `src/main/java/com/example/maschat/service/ChatService.java`
- **Thay đổi**: Cập nhật `isStaffEngaged` dựa trên agent được chọn
- **Logic**: 
  - Nếu chọn StaffAgent → `isStaffEngaged = true`
  - Nếu chọn agent khác → `isStaffEngaged = false`
  - Nếu không chọn agent → `isStaffEngaged = false`

### 5. Cập nhật logic trong `sendAgentResponse`
- **File**: `src/main/java/com/example/maschat/service/ChatService.java`
- **Thay đổi**: Kiểm tra thêm `isStaffEngaged` flag
- **Logic**: Bỏ qua phản hồi bot nếu `isStaffEngaged = true`

### 6. Cập nhật logic trong `checkForMissedResponses`
- **File**: `src/main/java/com/example/maschat/service/ChatService.java`
- **Thay đổi**: Kiểm tra `isStaffEngaged` trong scheduled task
- **Logic**: Bỏ qua cuộc trò chuyện có `isStaffEngaged = true`

## Migration Database

### File migration: `migration_add_is_staff_engaged.sql`
```sql
ALTER TABLE conversations ADD COLUMN is_staff_engaged BOOLEAN DEFAULT FALSE;
UPDATE conversations SET is_staff_engaged = FALSE WHERE is_staff_engaged IS NULL;
```

## Cách hoạt động

1. **Khi user yêu cầu nói chuyện với staff**:
   - `routeToStaffAgent()` được gọi
   - `isStaffEngaged` được set thành `true`
   - Bot ngừng phản hồi

2. **Khi staff thay đổi agent**:
   - `updateConversationAgents()` được gọi
   - Nếu chọn StaffAgent → `isStaffEngaged = true` (tiếp tục ngăn bot)
   - Nếu chọn agent khác → `isStaffEngaged = false` (cho phép bot phản hồi)

3. **Khi user gửi tin nhắn**:
   - Kiểm tra `isStaffEngaged` trước khi gửi phản hồi bot
   - Nếu `true` → bỏ qua phản hồi bot
   - Nếu `false` → gửi phản hồi bot bình thường

## Lợi ích

- **Ngăn bot phản hồi**: Khi staff đã tham gia, bot sẽ không can thiệp
- **Kiểm soát participant**: Chỉ staff mới có thể thay đổi agent
- **Linh hoạt**: Staff có thể chuyển đổi giữa staff mode và bot mode
- **Đồng bộ**: Tất cả các method đều kiểm tra flag này một cách nhất quán
