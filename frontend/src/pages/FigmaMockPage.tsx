const topActions = [
  'Создать новый проект',
  'Перейти в нормативы',
  'Перейти в документы',
];

const projects = [
  'Компрессионные чулки I класс',
  'Компрессионные чулки II класс',
  'Компрессионные чулки III класс',
  'Спрей от варикоза',
  'Батлер',
  'Чашки петри',
];

const chartData = [
  { label: '2026-P01', value: 3, color: '#10c0dd' },
  { label: '2026-P02', value: 24, color: '#49c8bd' },
  { label: '2026-P03', value: 63, color: '#a0d28d' },
  { label: '2026-P04', value: 79, color: '#c3d77a' },
  { label: '2026-P05', value: 81, color: '#e3db68' },
  { label: '2026-P06', value: 97, color: '#fbdd5b' },
];

const tasks = [
  { done: true, text: 'Заполнить паспорт риска для новой линейки компрессионных гольфов (класс I).' },
  { done: true, text: 'Сформировать PDF-отчет по критическим рискам производства для руководства.' },
  { done: false, text: 'Добавить нового эксперта в проект «Анализ силиконовых вставок».' },
  { done: false, text: 'Проверить связки рисков между разделами «Сырье» и «Брак вязки», добавить новые.' },
];

export default function FigmaMockPage() {
  return (
    <div className="min-h-screen bg-[linear-gradient(127deg,#f0fbff_0%,#eaeaea_100%)] text-black">
      <div className="mx-auto flex min-h-screen w-full max-w-[1600px]">
        <aside className="w-[320px] border-r border-black/10 bg-[#f0eece] px-5 py-5 shadow-[5px_0_10px_rgba(0,0,0,0.18)]">
          <div className="mb-7 rounded-md bg-[#f0eece] p-3 shadow-[0_4px_4px_rgba(0,0,0,0.2)]">
            <div className="flex items-center gap-3">
              <img
                src="https://www.figma.com/api/mcp/asset/2550c68d-f773-4c2b-88b1-2c95e6b85fb0"
                alt="avatar"
                className="h-14 w-14 rounded-full object-cover"
              />
              <div>
                <div className="text-[22px] font-bold leading-6">Анастасия Бакович</div>
                <div className="text-[16px] leading-5 text-black/75">Маркетолог</div>
              </div>
            </div>
          </div>

          <nav className="space-y-2 text-[28px] leading-none">
            <p className="py-1">Поиск</p>
            <p className="py-1 font-bold">Главная</p>
            <p className="py-1">Проекты</p>
            <div className="space-y-1 pl-5 text-[15px] leading-5 text-black/85">
              <p>2026-P01. Компрессионные чулки I класс</p>
              <p>2026-P02. Компрессионные чулки II класс</p>
              <p>2026-P03. Спрей от варикоза</p>
              <p>2026-P04. Бателер</p>
              <p>2026-P05. Чашки петри</p>
            </div>
            <p className="pt-2">Мессенджер</p>
            <p>Уведомления</p>
            <p>Документы</p>
            <p>Нормативы</p>
          </nav>

          <div className="mt-12 space-y-3 text-[28px]">
            <p>Пользователи</p>
            <p>Журнал изменений</p>
            <p>Настройки</p>
          </div>
        </aside>

        <main className="flex-1 p-6">
          <div className="mb-4 flex items-center justify-between">
            <div className="text-5xl leading-none text-black/50">+</div>
            <div className="text-[34px]">27.02.2026 11:25</div>
          </div>

          <div className="mb-6 grid grid-cols-3 gap-4">
            {topActions.map((action) => (
              <button
                key={action}
                className="rounded-[18px] border-[5px] border-[#aed486] bg-white px-4 py-4 text-[22px] font-bold underline decoration-1"
              >
                {action}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-[1.4fr_1fr_0.65fr] gap-4">
            <section className="rounded-[18px] bg-white p-5">
              <div className="mb-2 text-[38px] font-bold leading-10">Проект: Компрессионные чулки I класс</div>
              <div className="mb-4 text-xs text-black/70">Последние действия: 1 день назад</div>
              <div className="flex items-center justify-between">
                <button className="rounded-lg bg-[#dfeecf] px-5 py-2 text-base">Продолжить работу</button>
                <div className="relative h-40 w-40 rounded-full border-4 border-[#e7d9dc]">
                  <div className="absolute inset-0 rounded-full border-4 border-r-[#ef5a65] border-t-[#ef5a65] border-l-transparent border-b-transparent" />
                  <div className="absolute inset-0 flex items-center justify-center text-center text-xs">
                    <div>
                      <div className="text-lg">3 %</div>
                      <div>заполненных данных</div>
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <section className="rounded-[18px] bg-white p-5">
              <h2 className="mb-4 text-[36px] font-bold leading-9">Мои задачи на сегодня:</h2>
              <ul className="space-y-3 text-[17px] leading-5">
                {tasks.map((task, idx) => (
                  <li key={idx} className="flex gap-2">
                    <span className={`mt-1 h-2.5 w-2.5 rounded-full ${task.done ? 'bg-gray-300' : 'border border-black/40'}`} />
                    <span className={task.done ? 'line-through' : ''}>{task.text}</span>
                  </li>
                ))}
              </ul>
            </section>

            <section className="rounded-[18px] bg-white p-3">
              <h2 className="mb-3 text-center text-[34px] font-bold leading-9">Все проекты</h2>
              <div className="space-y-2">
                {projects.map((project) => (
                  <div key={project} className="rounded-lg bg-[#f5f5f5] p-2 text-center">
                    <div className="text-[13px] leading-4">{project}</div>
                    <div className="mt-2 h-1 rounded bg-gradient-to-r from-cyan-400 via-green-300 to-yellow-300" />
                  </div>
                ))}
              </div>
              <div className="mt-3 text-center text-xs underline">Перейти во все проекты...</div>
            </section>
          </div>

          <div className="mt-4 grid grid-cols-[1fr_1fr_0.65fr] gap-4">
            <div className="rounded-[18px] bg-white p-4">
              <div className="flex items-center">
                {[
                  '009a3f12-64da-4012-9234-2b4e07825022',
                  '3b1a08ef-94a9-4380-aece-6785ff4de8c2',
                  '938b7dcf-3ec4-463e-b408-73c5ccad9454',
                  'a298cea2-006a-47dc-8cd4-42f6cc5f2fd6',
                  'f035e797-d14b-4ad5-b3cc-6b8fcd914508',
                ].map((id, idx) => (
                  <img
                    key={id}
                    src={`https://www.figma.com/api/mcp/asset/${id}`}
                    alt=""
                    className={`h-10 w-10 rounded-full border-2 border-white object-cover ${idx > 0 ? '-ml-2' : ''}`}
                  />
                ))}
                <div className="-ml-2 flex h-10 w-10 items-center justify-center rounded-full bg-gray-400 text-white">+</div>
              </div>
            </div>

            <div className="rounded-[18px] bg-white p-4 text-center text-[32px] leading-9">
              Хорошего дня и отличного настроения!
            </div>
            <div />
          </div>

          <section className="mt-4 rounded-[18px] bg-white p-4">
            <div className="rounded-[18px] bg-[#f5f5f5] p-5">
              <div className="grid grid-cols-6 items-end gap-8">
                {chartData.map((item) => (
                  <div key={item.label} className="text-center">
                    <div className="mb-1 text-xs">{item.value} %</div>
                    <div className="mx-auto h-60 w-14 bg-black/10">
                      <div className="w-full" style={{ height: `${item.value}%`, backgroundColor: item.color }} />
                    </div>
                    <div className="mt-2 text-[10px]">{item.label}</div>
                  </div>
                ))}
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}
