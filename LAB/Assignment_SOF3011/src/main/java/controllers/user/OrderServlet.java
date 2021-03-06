package controllers.user;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.impl.OrderDaoImpl;
import dao.impl.OrderDetailDaoImpl;
import entitys._OrderDetails;
import entitys._Orders;
import entitys._Users;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet({
        "/client/add-order",
        "/admin/order/index",
        "/admin/order/edit",
        "/admin/order/update",
        "/admin/order/delete"
})
public class OrderServlet extends HttpServlet {
    OrderDao orderDao;
    OrderDetailDao orderDetailDao;

    public OrderServlet() {
        orderDao = new OrderDaoImpl();
        orderDetailDao = new OrderDetailDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.contains("add-order")){
            this.addOrder(request,response);
        }else if (uri.contains("admin/order/index")){
            this.index(request,response);
        }else if (uri.contains("admin/order/delete")){
            this.delete(request,response);
        }else if (uri.contains("admin/order/update")){
            this.update(request,response);
        }
        else {

        }
    }

    private void index(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
        List<_Orders> orders = orderDao.findAll();
        request.setAttribute("ds",orders);
        request.setAttribute("view1", "/views/admin/order/index.jsp");
        request.getRequestDispatcher("/views/layout.jsp").forward(request, response);
    }
    private void delete(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
        String id = request.getParameter("id");
        _Orders orders = orderDao.findById(Integer.valueOf(id));
        if (orders!=null && orders.getStatus()!=0){
            orders.setStatus(0);
            orderDao.delete(orders);
            response.sendRedirect("index");
        }
    }
    private void update(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
            String id = request.getParameter("id");
            _Orders orders = orderDao.findById(Integer.valueOf(id));
            if (orders!=null && orders.getStatus()!=2){
                orders.setStatus(2);
                orderDao.update(orders);
                response.sendRedirect("index");
            }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
    private void addOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            HttpSession session = request.getSession();
            //l???y danh s??ch s???n ph???m v???
            Object obj = session.getAttribute("cart");
            if (obj!= null){
                //??p th??nh map
                Map<String, _OrderDetails> map = (Map<String, _OrderDetails>) obj;
                _Orders order = new _Orders();
                _Users user= (_Users) session.getAttribute("user");
                order.setCusId(user);
                //t???o ????? l???y id
                orderDao.create(order);

                long total =0;
                //duy???t t???ng sp 1 v??o h??a ????n chi ti???t
                for (Map.Entry<String, _OrderDetails> entry: map.entrySet()){
                        _OrderDetails orderDetails =entry.getValue();
                        orderDetails.setOrdid(order);
                        orderDetailDao.create(orderDetails);
                        total+= orderDetails.getQuantity() * orderDetails.getPrice();
                }
                order.setTotal(total);
                orderDao.update(order);
                session.removeAttribute("cart");
                session.setAttribute("order-success","?????t ????n th??nh c??ng vui l??ng ?????i x??c nh???n");
                response.sendRedirect("index");
            }else {
                session.setAttribute("order-fail","?????t ????n kh??ng th??nh c??ng vui l??ng ch???n s???n ph???m mu???n mua");
                response.sendRedirect("index");
            }
    }
}
