import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. 큰 바깥 창(JFrame) 생성
        JFrame frame = new JFrame("2D 움직이는 카메라 슈팅 게임");

        // 2. 우리가 만든 화면 도화지(GamePanel) 객체 생성
        GamePanel gamePanel = new GamePanel();

        // 3. 창에 도화지 조립하기
        frame.add(gamePanel);

        // 4. 창 세팅 및 노출
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}