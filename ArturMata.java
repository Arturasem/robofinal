package ArturFinal;

import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.util.Utils;

public class ArturMata extends RateControlRobot {
    
    // Método principal chamado quando o robô inicia
    public void run() {
        // Configurações para ajustar o radar, arma e robô
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
		
		setBodyColor(Color.RED);  // Cor do chassi 
		setGunColor(Color.GREEN); // Cor da arma
		setRadarColor(Color.ORANGE); // Cor do radar
		setBulletColor(Color.YELLOW); // Cor das balas disparadas pelo robô
		setScanColor(Color.ORANGE); // Cor do scanner

        // Loop principal do robô
        while (true) {
            setVelocityRate(5); // Define a velocidade do robô
            setTurnRateRadians(0); // Define a taxa de rotação do robô
            execute(); // Executa os comandos
            turnRadarRight(360); // Gira o radar completo
        }
    }
    
    // Método chamado quando um inimigo é detectado pelo radar
    public void onScannedRobot(ScannedRobotEvent e) {
        // Cálculo da potência do tiro (limitada a 2.0)
        double power = Math.min(2.0, getEnergy());
        
        // Cálculos para prever a posição do inimigo
        double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(enemyAbsoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(enemyAbsoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyVelocity = e.getVelocity();
        double battlefieldHeight = getBattleFieldHeight();
        double battlefieldWidth = getBattleFieldWidth();
        double predictedX = enemyX;
        double predictedY = enemyY;
        
        predictedX += Math.sin(enemyHeading) * enemyVelocity;
        predictedY += Math.cos(enemyHeading) * enemyVelocity;
        
        // Garante que o alvo previsto esteja dentro dos limites do campo de batalha
        if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battlefieldWidth - 18.0 || predictedY > battlefieldHeight - 18.0) {
            predictedX = Math.min(Math.max(18.0, predictedX), battlefieldWidth - 18.0);
            predictedY = Math.min(Math.max(18.0, predictedY), battlefieldHeight - 18.0);
        }
        
        // Cálculo do ângulo absoluto para mirar no inimigo
        double absoluteAngle = Utils.normalAbsoluteAngle(
            Math.atan2(
                predictedX - getX(), predictedY - getY()
            )
        );
        
        // Ajusta a direção do radar, arma e atira
        setTurnRightRadians(enemyAbsoluteBearing / 2 * -1 - getRadarHeadingRadians());
        setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteAngle - getGunHeadingRadians()));
        fire(power); // Atira com a potência calculada
        
        // Lógica para ajustar a velocidade e taxa de rotação do robô
        if (getVelocityRate() > 0){
            setVelocityRate(getVelocityRate() + 1);
        } 
        else {
            setVelocityRate(- 1);
        }

        if (getVelocityRate() > 0 && ((getTurnRate() < 0 && enemyAbsoluteBearing > 0) || (getTurnRate() > 0 && enemyAbsoluteBearing < 0))) {
            setTurnRate(getTurnRate() * -1);
        }
    }
    
    // Método chamado quando o robô é atingido por uma bala
    public void onHitByBullet(HitByBulletEvent e) {
        // Ajusta o radar e a direção do robô para evitar novos tiros
        double radarTurn = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading());
        setTurnRadarRight(radarTurn);
        setTurnLeft(-3);
        setTurnRate(3);
        setVelocityRate(-1 * getVelocityRate());
    }
    
    // Método chamado quando o robô colide com uma parede
    public void onHitWall(HitWallEvent e) {
        // Inverte a direção do robô e ajusta a taxa de rotação
        setVelocityRate(-1 * getVelocityRate());
        setTurnRate(getTurnRate() + 2);
        execute();
    }
    
    // Método chamado quando o robô colide com outro robô
    public void onHitRobot(HitRobotEvent e) {
        // Ajusta a direção da arma e atira, além de ajustar a velocidade
        double gunTurn = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
        turnGunRight(gunTurn);
        setFire(3);
        setVelocityRate(getVelocity() + 3);
        execute();
    }
}