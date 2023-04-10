import hwuichi from './utils/Hwuichi';

const context = describe;

describe('api', () => {
  describe('POST /tasks', () => {
    beforeEach(async () => {
      const { data } = await hwuichi.get('/tasks');

      await Promise.all(
        Object.keys(data).map((id) => hwuichi.delete(`/tasks/${id}`)),
      );
    });

    context('정상 동작', () => {
      it('201 Created 응답을 받음', async () => {
        const {
          statusCode, data,
        } = await hwuichi.post('/tasks', { task: 'studying' });

        const tasks = Object.values(data);

        expect(statusCode).toEqual(201);
        expect(tasks).toContain('studying');
      });
    });

    context('Body data가 없을 때', () => {
      it('400 Bad Request 응답을 받음', async () => {
        const { statusCode } = await hwuichi.post('/tasks');

        expect(statusCode).toEqual(400);
      });
    });
  });

  describe('GET /tasks', () => {
    context('할 일이 하나도 없을 때', () => {
      beforeEach(async () => {
        const { data } = await hwuichi.get('/tasks');

        await Promise.all(
          Object.keys(data).map((id) => hwuichi.delete(`/tasks/${id}`)),
        );
      });

      it('200 OK 응답을 받음', async () => {
        const { statusCode, data } = await hwuichi.get('/tasks');

        expect(statusCode).toEqual(200);
        expect(data).toEqual({});
      });
    });

    context('할 일들이 있을 때', () => {
      const taskList = ['working', 'shooting', 'shopping'];

      beforeEach(async () => {
        await Promise.all(
          taskList.map((task) => hwuichi.post('/tasks', { task })),
        );
      });

      it('200 OK 응답을 받음', async () => {
        const { statusCode, data } = await hwuichi.get('/tasks');

        const tasks = Object.values(data);

        expect(statusCode).toEqual(200);
        taskList.forEach((task) => expect(tasks).toContain(task));
      });
    });
  });

  describe('PATCH /tasks/{id}', () => {
    context('정상 동작', () => {
      it('200 OK 응답을 받음', async () => {
        const oldTask = 'exercising';
        const newTask = 'sleeping';

        await hwuichi.post('/tasks', { task: oldTask });

        const response = await hwuichi.get('/tasks');

        const ids = Object.keys(response.data);
        const id = ids[ids.length - 1];

        const oldTasks = Object.values(response.data);

        expect(oldTasks).toContain(oldTask);

        const {
          statusCode, data,
        } = await hwuichi.patch(`/tasks/${id}`, { task: newTask });

        expect(statusCode).toEqual(200);

        const newTasks = Object.values(data);

        expect(newTasks).toContain(newTask);
      });
    });

    context('존재하지 않는 ID 값일 때', () => {
      it('404 Not Found 응답을 받음', async () => {
        const {
          statusCode,
        } = await hwuichi.patch('/tasks/-1', { task: 'walking' });

        expect(statusCode).toEqual(404);
      });
    });

    context('Body data가 없을 때', () => {
      it('400 Bad Request 응답을 받음', async () => {
        await hwuichi.post('/tasks', { task: 'climbing' });

        const response = await hwuichi.get('/tasks');

        const ids = Object.keys(response.data);
        const id = ids[ids.length - 1];

        const { statusCode } = await hwuichi.patch(`/tasks/${id}`);

        expect(statusCode).toEqual(400);
      });
    });
  });

  describe('DELETE /task/{id}', () => {
    context('정상 동작', () => {
      it('200 OK 응답을 받음', async () => {
        await hwuichi.post('/tasks', { task: 'nothing' });

        const response = await hwuichi.get('/tasks');

        const ids = Object.keys(response.data);
        const id = ids[ids.length - 1];

        const oldTasks = Object.values(response.data);

        expect(oldTasks).toContain('nothing');

        const {
          statusCode, data,
        } = await hwuichi.delete(`/tasks/${id}`);

        expect(statusCode).toEqual(200);

        const newTasks = Object.values(data);

        expect(newTasks).not.toContain('nothing');
      });
    });

    context('존재하지 않는 ID 값일 때', () => {
      it('404 Not Found 응답을 받음', async () => {
        const {
          statusCode,
        } = await hwuichi.delete('/tasks/1');

        expect(statusCode).toEqual(404);
      });
    });
  });
});
