package co.edu.unicauca.bancopreguntas;

import co.edu.unicauca.bancopreguntas.access.QuestionImplRepository;
import co.edu.unicauca.bancopreguntas.domain.QuestionRepository;
import co.edu.unicauca.bancopreguntas.domain.QuestionService;
import co.edu.unicauca.bancopreguntas.presentation.GUIObserver1;
import co.edu.unicauca.bancopreguntas.presentation.GUIObserver2;
import co.edu.unicauca.bancopreguntas.presentation.GUIQuestions;
import co.edu.unicauca.bancopreguntas.presentation.QuestionController;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicacion.
 *
 * Es el unico lugar donde se conocen las implementaciones concretas: aqui se
 * ensamblan las capas (composition root) y se inyectan las dependencias, de
 * modo que ninguna capa instancia por su cuenta a la de abajo.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    /**
     * Ensambla las capas, registra las vistas observadoras y muestra la interfaz.
     */
    private static void launch() {
        applyLookAndFeel();

        // Capa de acceso a datos.
        QuestionRepository repository = new QuestionImplRepository();

        // Capa de dominio: modelo del MVC y sujeto observable. Es el unico
        // punto donde se usa el tipo concreto, porque aqui se resuelven las
        // dependencias y se registran los observadores.
        QuestionService service = new QuestionService(repository);

        // Capa de presentacion: el controlador solo ve la abstraccion
        // IQuestionService, no la implementacion.
        QuestionController controller = new QuestionController(service);
        GUIQuestions mainView = new GUIQuestions(controller);
        GUIObserver1 statisticsView = new GUIObserver1();
        GUIObserver2 chartView = new GUIObserver2();

        // Suscripcion de las vistas al sujeto (patron Observer).
        service.addObserver(statisticsView);
        service.addObserver(chartView);

        // Primer render con el estado inicial del banco de preguntas.
        service.notifyAllObservers(service.getStatistics());

        mainView.setVisible(true);
        statisticsView.setVisible(true);
        chartView.setVisible(true);
    }

    /**
     * Intenta usar la apariencia nativa del sistema; si falla, conserva la
     * apariencia por omision de Swing.
     */
    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("No fue posible aplicar la apariencia del sistema: " + ex.getMessage());
        }
    }
}
