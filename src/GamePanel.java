import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Random;

public class GamePanel extends JPanel {
    private final Enemy enemy;

    // 이미지를 담을 변수들
    private Image normalImage;
    private Image damagedImage;

    // 카메라 시점 이동량 (Offset)
    private int viewOffsetX = 0;
    private int viewOffsetY = 0;

    // 시점 제한 범위
    private final int LIMIT_MIN = 100;
    private final int LIMIT_MAX = 400;

    private boolean isFlashing = false;

    // 💡 [핵심 추가] 2초간 중복 클릭을 막기 위한 플래그 변수 (디바운스/쿨타임)
    private boolean isReadyToShoot = true;

    private String logMessage = "🎯 마우스를 움직여 적을 찾고 쏘세요!";
    private final Random rand = new Random();

    public GamePanel() {
        this.enemy = new Enemy();

        // 1. 원래 사진 로드
        java.net.URL normalURL = GamePanel.class.getResource("enemy.png");
        if (normalURL != null) {
            this.normalImage = new ImageIcon(normalURL).getImage();
        } else {
            System.err.println("❌ [경로 에러] enemy.jpg 파일을 찾을 수 없습니다!");
        }

        // 2. 찌그러진 사진 로드
        java.net.URL damagedURL = GamePanel.class.getResource("enemy2.png");
        if (damagedURL != null) {
            this.damagedImage = new ImageIcon(damagedURL).getImage();
        } else {
            System.err.println("❌ [경로 에러] enemy_damaged.png 파일을 찾을 수 없습니다!");
            this.damagedImage = this.normalImage;
        }

        // 1. 마우스 클릭 이벤트 처리 (사격 및 판정)
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (enemy.isDead()) return;

                // 💡 [요구사항] 2초 쿨타임 체크: 아직 준비가 안 되었다면 사격 로직을 통과시킵니다.
                if (!isReadyToShoot) {
                    return;
                }

                // 💡 사격 시작과 동시에 문을 걸어잠급니다 (2초 락온)
                isReadyToShoot = false;
                isFlashing = true; // 빨간 화면 켜기
                enemy.setDamaged(true); // 적 이미지 찌그러트리기
                repaint();

                // 좌표 보정
                int worldMouseX = e.getX() - viewOffsetX;
                int worldMouseY = e.getY() - viewOffsetY;

                // 조준점과 적 중심점 사이의 거리 계산
                double distance = Math.sqrt(
                        Math.pow(worldMouseX - enemy.getX(), 2) + Math.pow(worldMouseY - enemy.getY(), 2)
                );

                // 명중 판정
                if (distance <= enemy.getCollisionRadius()) {
                    int damage = rand.nextInt(15) + 15;
                    enemy.takeDamage(damage);

                    if (enemy.isDead()) {
                        logMessage = "🎉 미션 성공! 적을 완전히 찌그러트렸습니다.";
                    } else {
                        logMessage = "💥 명중! " + damage + " 데미지! (재장전 중... 2초)";
                        enemy.moveRandomly();
                    }
                } else {
                    logMessage = "💨 빗나갔습니다! (허공) (재장전 중... 2초)";
                }

                // 💡 [요구사항] 2초(2000ms) 후에 원래 상태로 복구하는 타이머
                Timer cooldownTimer = new Timer(2000, event -> {
                    isFlashing = false; // 빨간 화면 끄기

                    // 적이 살아있을 때만 원래 이미지로 복구하고 다시 쏠 수 있게 만듬
                    if (!enemy.isDead()) {
                        enemy.setDamaged(false);
                        isReadyToShoot = true; // 💡 문을 다시 열어줍니다 (사격 가능)
                        logMessage = "🎯 장전 완료! 다시 쏠 수 있습니다.";
                    }
                    repaint();
                });
                cooldownTimer.setRepeats(false); // 딱 한 번만 실행
                cooldownTimer.start();
            }
        };
        addMouseListener(mouseAdapter);

        // 2. 마우스 움직임 이벤트 처리 (역방향 카메라 이동)
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int currentMouseX = e.getX();
                int currentMouseY = e.getY();

                int clampedX = Math.max(LIMIT_MIN, Math.min(LIMIT_MAX, currentMouseX));
                int clampedY = Math.max(LIMIT_MIN, Math.min(LIMIT_MAX, currentMouseY));

                viewOffsetX = -(clampedX - 250);
                viewOffsetY = -(clampedY - 250);

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {}
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- A. 화면 평행이동 ---
        g2d.translate(viewOffsetX, viewOffsetY);

        // --- B. 월드 그래픽 영역 ---
        g2d.setColor(new Color(245, 245, 240));
        g2d.fillRect(-250, -250, 1000, 1000);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(-LIMIT_MIN, -LIMIT_MIN, LIMIT_MAX + (LIMIT_MIN * 2), LIMIT_MAX + (LIMIT_MIN * 2));

        g2d.setColor(new Color(230, 230, 230));
        g2d.setStroke(new BasicStroke(1));
        for (int i = -250; i <= 750; i += 50) {
            g2d.drawLine(i, -250, i, 750);
            g2d.drawLine(-250, i, 750, i);
        }

        // 적 캐릭터 이미지 그리기
        int ex = enemy.getX();
        int ey = enemy.getY();
        int ew = enemy.getWidth();
        int eh = enemy.getHeight();

        if (enemy.isDead() || enemy.isDamaged()) {
            if (damagedImage != null) {
                g2d.drawImage(damagedImage, ex - (ew / 2), ey - (eh / 2), ew, eh, this);
            }
        } else {
            if (normalImage != null) {
                g2d.drawImage(normalImage, ex - (ew / 2), ey - (eh / 2), ew, eh, this);
            }
        }

        if (enemy.isDead()) {
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
            g2d.setColor(Color.RED);
            g2d.drawString("K.O.", ex - 30, ey + 10);
        }

        // 💡 총격 피격 시 맵 전체 붉은색 필터 (이제 2초간 켜져 있습니다)
        if (isFlashing) {
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.fillRect(-250, -250, 1000, 1000);
        }

        // --- C. 평행이동 해제 ---
        g2d.translate(-viewOffsetX, -viewOffsetY);

        // --- D. 고정 상단 UI 영역 ---
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), 90);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        g2d.drawString("❤️ 적 체력: " + enemy.getHp() + " / 100", 20, 40);

        // 체력 바
        g2d.setColor(Color.GRAY);
        g2d.fillRect(180, 25, 200, 20);
        g2d.setColor(Color.RED);
        g2d.fillRect(180, 25, enemy.getHp() * 2, 20);

        // 💡 재장전 중(쿨타임)일 때는 쿨타임 게이지나 텍스트 상태를 직관적으로 표현
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        if (!isReadyToShoot && !enemy.isDead()) {
            g2d.setColor(Color.YELLOW); // 쿨타임 중에는 노란 글씨로 경고
        }
        g2d.drawString(logMessage, 20, 70);

        // 미션 클리어 알림
        if (enemy.isDead()) {
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            g2d.setColor(new Color(50, 150, 250));
            g2d.drawString("🎯 MISSION CLEAR", 120, 250);
        }
    }
}