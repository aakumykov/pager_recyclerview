# pager_recyclerview
RecyclerView, сообщающий о событиях смены "страницы" через коллбеки. Выдаёт события "страница добавлена", "страница удалена", "смена страницы", передавая при этом объект Page, который содержит элемент списка и соответствующий ему ViewHolder.
Создан для использования в имитации ViewPager-а средствами RecyclerView: элемент отображается во весь экран, ориентация горизонтальная, подключен PagerSnapHelper. Пример использования находится в ветке demo.

RecyclerView that reports about page change events via callback. It rises following events: "page added", "page removed", "page changed" with Page object. Page object contains list item and corresponding ViewHolder.
It was created for use in ViewPager-like RecyclerView: item is shown on fullscreen, horizontal orientation, PagerSnapHelper is bounded. There is a demo in "demo" branch.
