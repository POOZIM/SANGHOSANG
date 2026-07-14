import java.util.Random;

public class Enemy {
    private int hp = 100;
    private int x = 250;
    private int y = 250;

    // 설정하신 비율 맞춘 크기
    private final int width = 169;
    private final int height = 192;

    private final Random rand = new Random();

    // 💡 [추가] 적의 현재 상태를 나타내는 변수들
    private boolean isDamaged = false; // 지금 맞아서 찌그러진 상태인가?
    private boolean isDead = false;    // 체력이 다 닳아 죽었는가?

    public void takeDamage(int damage) {
        if (isDead) return; // 이미 죽었으면 데미지를 받지 않음

        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isDead = true; // 💡 죽음 상태로 변경
        }
    }

    public void moveRandomly() {
        if (isDead) return; // 죽었으면 움직이지 않음
        // 이미지가 화면 가장자리에 걸치지 않도록 범위 살짝 조정
        this.x = rand.nextInt(350) + 75; // 75 ~ 425 사이
        this.y = rand.nextInt(350) + 100; // 100 ~ 450 사이
    }

    // [💡 핵심 수정] 클릭 판정을 위한 '충돌 반지름' 계산
    public int getCollisionRadius() {
        // 얼굴 부위 조준을 위해 크기의 절반보다 약간 작게 잡는 것이 타격감이 좋습니다.
        return Math.min(width, height) / 3;
    }

    // 💡 [추가] 상태 변수들에 대한 Getter/Setter
    public int getHp() { return hp; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isDamaged() { return isDamaged; }
    public void setDamaged(boolean damaged) { isDamaged = damaged; } // 잠깐 찌그러트릴 때 사용
    public boolean isDead() { return isDead; }
}